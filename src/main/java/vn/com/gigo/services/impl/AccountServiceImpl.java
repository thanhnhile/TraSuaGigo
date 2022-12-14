package vn.com.gigo.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.com.gigo.dtos.AccountDto;
import vn.com.gigo.dtos.AccountNoPassDto;
import vn.com.gigo.dtos.EmployeeDto;
import vn.com.gigo.entities.Account;
import vn.com.gigo.entities.Product;
import vn.com.gigo.entities.Role;
import vn.com.gigo.exception.AccountException;
import vn.com.gigo.exception.DuplicateValueInResourceException;
import vn.com.gigo.mapstruct.AccountMapper;
import vn.com.gigo.mapstruct.CustomerMapper;
import vn.com.gigo.mapstruct.EmployeeMapper;
import vn.com.gigo.repositories.AccountRepository;
import vn.com.gigo.repositories.EmployeeRepository;
import vn.com.gigo.repositories.RoleRepository;
import vn.com.gigo.services.AccountService;
import vn.com.gigo.utils.RoleType;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AccountRepository accountRepo;
	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private CustomerMapper customerMapper;

	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private EmployeeRepository employeeRepo;
	
	@Autowired
	private EmployeeMapper employeeMapper;

	@Autowired
	public AccountServiceImpl(AccountMapper accountMapper, AccountRepository accountRepo) {
		this.accountMapper = accountMapper;
		this.accountRepo = accountRepo;
	}

	@Override
	public Object getAccount(Long id) {
		Account account = accountRepo.findOneById(id);
		if (account == null)
			throw new AccountException(id);

		Set<String> rolenames = new HashSet<String>();
		for (Role role : account.getRoles()) {
			rolenames.add(role.getName());
		}
		AccountNoPassDto accountNoPassDto = accountMapper.accountToAccountNoPassDto(account);
		accountNoPassDto.setRole(rolenames);
		return accountNoPassDto;
	}

	@Override
	public Object getAllAccount() {
		List<Account> accounts = new ArrayList<>();
		accountRepo.findAll().forEach(accounts::add);
		List<AccountNoPassDto> accountNoPassDto = accountMapper.accountsToAccountNoPassDtos(accounts);
		for (AccountNoPassDto accountDto : accountNoPassDto) {
			Set<String> rolenames = new HashSet<String>();
			for (Account account : accounts) {
				if (accountDto.getUsername().equals(account.getUsername())) {
					for (Role role : account.getRoles()) {
						rolenames.add(role.getName());
					}
				}

			}
			accountDto.setRole(rolenames);
		}

		return accountNoPassDto;
	}

	@Override
	public Object addAccount(AccountDto accountDto) {
		if (checkExistedAccount(accountDto.getUsername())) {
			throw new DuplicateValueInResourceException("S??? ??i???n tho???i ???? t???n t???i");
		}
		Account account = accountMapper.accountDtoToAccount(accountDto);
		String rawPassword = account.getPassword();
		String encodedPassword = passwordEncoder.encode(rawPassword);// thuat toan ma hoa BCrypt
		account.setPassword(encodedPassword);
		account.setCustomer(null);
		Role roleUser = roleRepository.findOneById(RoleType.ROLE_USER.getValue());
		account.getRoles().add(roleUser);
		accountRepo.save(account);
		return accountDto;
	}

	public Boolean checkExistedAccount(String username) {
		Account account = accountRepo.findOneByUsername(username);
		if (account != null) {
			return true;
		}
		return false;
	}

	@Override
	public void deleteAccount(Long id) {
		accountRepo.delete(accountRepo.findOneById(id));

	}

	@Override
	public Object updateAccount(Long id, AccountDto accountDto) {
		Account accountNew = accountMapper.accountDtoToAccount(accountDto);
		Account accountOld = accountRepo.findOneById(id);
		String rawPassword = accountNew.getPassword();
		String encodedPassword = passwordEncoder.encode(rawPassword);
		accountOld.setPassword(encodedPassword);
		return accountRepo.save(accountOld);
	}

	@Override
	public Object getCustomerInfoByUserName(String username) {
		Account account = accountRepo.findOneByUsername(username);
		if (account != null) {
			return customerMapper.customerToCustomerDto(account.getCustomer());
		}
		return null;
	}

	@Override
	public Object addRoleEmployee(String username) {
		// TODO Auto-generated method stub
		Account account = accountRepo.findOneByUsername(username);
		if (account != null) {
			Role roleEmployee = roleRepository.findOneById(RoleType.ROLE_EMPLOYEE.getValue());
			account.getRoles().add(roleEmployee);
			return accountMapper.accountToAccountDto(accountRepo.save(account));
		}
		return null;
	}

	@Override
	public Object removeRoleEmployee(Long id) {
		EmployeeDto employee = employeeMapper.employeeToEmployeeDto(employeeRepo.findOneById(id));
		Account account = accountRepo.findOneByUsername(employee.getAccount());
		if (account != null) {
			Role roleEmployee = roleRepository.findOneById(RoleType.ROLE_EMPLOYEE.getValue());
			account.getRoles().remove(roleEmployee);
			return accountMapper.accountToAccountDto(accountRepo.save(account));
		}
		return null;
	}

}
