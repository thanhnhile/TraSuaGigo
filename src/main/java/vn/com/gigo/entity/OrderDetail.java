package vn.com.gigo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name = "order_details")
public class OrderDetail {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private Double price;

	@Column(columnDefinition="int default 0")
	private int quantity;
	
	@OneToOne
	@JoinColumn(name = "product_id")
	private Product product;
	
	@ManyToOne()
	@JoinColumn(name="order_id")
	private Order order;

	public OrderDetail() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public OrderDetail(Double price, int quantity, Product product) {
		super();
		this.price = price;
		this.quantity = quantity;
		this.product = product;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
	

}
