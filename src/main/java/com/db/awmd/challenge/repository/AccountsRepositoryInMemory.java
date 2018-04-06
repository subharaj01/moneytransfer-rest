package com.db.awmd.challenge.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.UserTransaction;
import com.db.awmd.challenge.exception.AccountNotExistException;
import com.db.awmd.challenge.exception.BalanceNotSufficientException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.EmailNotificationService;
import com.db.awmd.challenge.service.NotificationService;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();


	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public void performTransaction(UserTransaction userTransaction)
			throws AccountNotExistException, BalanceNotSufficientException {
		
		//get meta data from transaction details
		String accountFrom = userTransaction.getAccountFromId();
		String accountTo = userTransaction.getAccountToId();
		BigDecimal amount = userTransaction.getAmount();
		
		//validation for account from which transfer happen
		if (accounts.get(accountFrom) == null) {
			throw new AccountNotExistException("account does not exist! account id = "+accountFrom);
		}
		//validation for account to which transfer happen
		if (accounts.get(accountTo) == null) {
			throw new AccountNotExistException("account does not exist! account id = "+accountTo);
		}
		
		NotificationService notificationService = new EmailNotificationService();
		
		accounts.computeIfPresent(accountFrom, (K,V) -> 
		{
			if (V.getBalance().compareTo(amount) == -1 ) {
				throw new BalanceNotSufficientException("balance is no"
						+ "t sufficient in account id = "+K);
			}
			V.setBalance(V.getBalance().subtract(amount));
			notificationService.notifyAboutTransfer(V,"your account has been debi*ted with amount "+amount);
			return V;
		});
		
		accounts.computeIfPresent(accountTo, (K,V) -> 
		{
			V.setBalance(V.getBalance().add(amount));
			notificationService.notifyAboutTransfer(V,"your account has been credited with amount "+amount);
			return V;
		});
	}
}
