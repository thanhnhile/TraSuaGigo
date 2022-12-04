package vn.com.gigo.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.com.gigo.dtos.OrderDetailDto;
import vn.com.gigo.dtos.OrderInputDto;
import vn.com.gigo.dtos.PagingDto;
import vn.com.gigo.entities.Order;
import vn.com.gigo.entities.OrderDetail;
import vn.com.gigo.mapstruct.OrderMapper;
import vn.com.gigo.repositories.CustomerRepository;
import vn.com.gigo.repositories.EmployeeRepository;
import vn.com.gigo.repositories.OrderDetailRepository;
import vn.com.gigo.repositories.OrderRepository;
import vn.com.gigo.repositories.StoreRepository;
import vn.com.gigo.services.OrderService;
import vn.com.gigo.utils.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService{

	@Autowired
	private OrderRepository orderRepo;
	
	@Autowired
	private OrderDetailRepository orderDetailRepo;
	
	@Autowired
	private CustomerRepository customerRepo;
	
	@Autowired
	private EmployeeRepository employeeRepo;
	
	@Autowired
	private StoreRepository storeRepo;
	
	@Autowired
	private OrderMapper mapper;
	
	@Override
	public Object getOrder(Long id) {
		return mapper.orderToOrderDto(orderRepo.getReferenceById(id));
	}

	@Override
	public Object addOrder(OrderInputDto orderInputDto) {
		Order orderToAdd = mapper.orderInputDtoToOrder(orderInputDto);
		orderToAdd.setStore(storeRepo.getReferenceById(orderInputDto.getStore()));
		orderToAdd.setEmployee(employeeRepo.getReferenceById(orderInputDto.getEmployee()));
		//set default property
		if(orderInputDto.getCustomer() != null) {
			orderToAdd.setCustomer(customerRepo.getReferenceById(orderInputDto.getCustomer()));
		}
		else orderToAdd.setCustomer(null);
		if(orderToAdd.getPay() == null) {
			orderToAdd.setPay(false);
		}
		orderToAdd.setStatus(OrderStatus.IN_PROGRESS.getValue());
		//caculate total
		Double total = 0.0;
		//save order detail
		List<OrderDetailDto> detailDtos = orderInputDto.getDetailList();
		orderToAdd.setDetailList(null);
		//save order
		Order newOrder = orderRepo.save(orderToAdd);
		List<OrderDetail> details = new ArrayList<OrderDetail>();
		for(OrderDetailDto detailDto : detailDtos) {
			OrderDetail orderDetail = mapper.detailDtoToDetail(detailDto);
			orderDetail.setOrder(newOrder);
			orderDetailRepo.save(orderDetail);
			details.add(orderDetail);
			total += detailDto.getPrice() * detailDto.getQuantity();
		}
		newOrder.setDetailList(details);
		newOrder.setTotal(total);
		newOrder = orderRepo.save(newOrder);
		return mapper.orderToOrderDto(newOrder);
	}

	@Override
	public Object deleteOrder(Long id) {
		Optional<Order> orderOptional = orderRepo.findById(id);
		if(orderOptional.isPresent()) {
			orderRepo.deleteById(id);
			return "Deleted";
		}
		return "Not found order with id "+id;
	}

	@Override
	public Object getAllOrdersByStoreId(Long storeId, int offSet, int limit) {
		Pageable pageable = PageRequest.of(offSet-1, limit);
		Page<Order> page = orderRepo.getOrdersByStoreId(storeId,pageable);
		PagingDto response = new PagingDto();
		response.setContent(mapper.ordersToOrderDtos(page.getContent()));
		response.setCurrentPage(offSet);
		response.setTotalElements(page.getTotalElements());
		response.setTotalPages(page.getTotalPages());
		return response;
	}

	@Override
	public Object updateOrderStatus(Long id, int status) {
		Optional<Order> orderOptional = orderRepo.findById(id);
		if(orderOptional.isPresent()) {
			Order orderToUpdate = orderOptional.get();
			orderToUpdate.setStatus(status);
			return mapper.orderToOrderDto(orderRepo.save(orderToUpdate));
		}
		else return "Not found order with id " +id;
	}

	@Override
	public Object updatePayStatus(Long id) {
		Optional<Order> orderOptional = orderRepo.findById(id);
		if(orderOptional.isPresent()) {
			Order orderToUpdate = orderOptional.get();
			orderToUpdate.setPay(true);
			return mapper.orderToOrderDto(orderRepo.save(orderToUpdate));
		}
		else return "Not found order with id " +id;
	}

	@Override
	public Object updateOrderDetail(Long id, List<OrderDetailDto> detailDtos) {
		Optional<Order> orderOptional = orderRepo.findById(id);
		if(orderOptional.isPresent()) {
			Order orderToUpdate = orderOptional.get();
			Double total = 0.0;
			List<OrderDetail> newDetails = new ArrayList<OrderDetail>();
			for(OrderDetailDto detailDto : detailDtos) {
				OrderDetail orderDetail = mapper.detailDtoToDetail(detailDto);
				orderDetail.setOrder(orderToUpdate);
				orderDetailRepo.save(orderDetail);
				newDetails.add(orderDetail);
				total += detailDto.getPrice() * detailDto.getQuantity();
			}
			orderToUpdate.setDetailList(newDetails);
			orderToUpdate.setTotal(total);
			return mapper.orderToOrderDto(orderRepo.save(orderToUpdate));
		}
		else return "Not found order with id " +id;
	}


	

}