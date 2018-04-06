package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	@Test
	public void createAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-123");
		assertThat(account.getAccountId()).isEqualTo("Id-123");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}

	@Test
	public void createDuplicateAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"balance\":1000}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoBalance() throws Exception {
		this.mockMvc.perform(
				post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"accountId\":\"Id-123\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoBody() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNegativeBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountEmptyAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void getAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId)).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
	}

	@Test
	public void transferMoneyWhenAccountFromIdDoesNotExist() throws Exception {

		// create accountToId only, don't create accountFromId
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"111\",\"balance\":1000}")).andExpect(status().isCreated());

		// try to transact from non existing accountFromId which should be
		// considered as bad request
		this.mockMvc
				.perform(post("/v1/accounts/transaction").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"111\",\"amount\":2}"))
				.andExpect(status().isBadRequest());

	}

	@Test
	public void transferMoneyWhenAccountToIdDoesNotExist() throws Exception {

		// create accountFromId only, don't create accountToId
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"111\",\"balance\":1000}")).andExpect(status().isCreated());

		// try to transact to non existing accountToId which should be
		// considered as bad request
		this.mockMvc
				.perform(post("/v1/accounts/transaction").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"111\",\"accountToId\":\"Id-123\",\"amount\":2}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void transferMoneyWhenAmountNegative() throws Exception {

		// create accountFromId and accountToId
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"111\",\"balance\":1000}")).andExpect(status().isCreated());
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"123\",\"balance\":1000}")).andExpect(status().isCreated());

		// try to transact negative amount which should be considered as bad
		// request
		this.mockMvc
				.perform(post("/v1/accounts/transaction").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"111\",\"accountToId\":\"123\",\"amount\":-1}"))
				.andExpect(status().isBadRequest());

		// try to transact negative amount and verify there is expected error in
		// result
		MvcResult result = this.mockMvc.perform(post("/v1/accounts/transaction").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountFromId\":\"111\",\"accountToId\":\"123\",\"amount\":-1}")).andReturn();

		Assert.assertNotNull("There should be exception due to bad amount", result.getResolvedException());
		Assert.assertTrue("There should be error message as Requested transaction amount cant be nagative",
				result.getResolvedException().getMessage().contains("Requested transaction amount cant be nagative"));
	}

	@Test
	public void transferMoneyWhenAmountZero() throws Exception {

		// create account = 111 (accountFromId)
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"111\",\"balance\":1000}")).andExpect(status().isCreated());

		// create account = 123 (accountToId)
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"123\",\"balance\":2000}")).andExpect(status().isCreated());

		// transact zero amount which should be considered as good request as
		// per current implementation
		this.mockMvc
				.perform(post("/v1/accounts/transaction").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"111\",\"accountToId\":\"123\",\"amount\":0}"))
				.andExpect(status().isOk());

		// verify balance in account = 111 (accountFromId) and it should be same
		this.mockMvc.perform(get("/v1/accounts/" + "111")).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + "111" + "\",\"balance\":1000}"));

		// verify balance in account = 123 (accountToId) and it should be same
		this.mockMvc.perform(get("/v1/accounts/" + "123")).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + "123" + "\",\"balance\":2000}"));
	}

	@Test
	public void transferMoneyWhenAmountPositive() throws Exception {

		// create account = 111 (accountFromId)
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"111\",\"balance\":1000}")).andExpect(status().isCreated());

		// create account = 123 (accountToId)
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"123\",\"balance\":2000}")).andExpect(status().isCreated());

		// transact zero amount which should be considered as good request as
		// per current implementation
		this.mockMvc
				.perform(post("/v1/accounts/transaction").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"111\",\"accountToId\":\"123\",\"amount\":500}"))
				.andExpect(status().isOk());

		// verify balance in account = 111 (accountFromId) and it should be 500
		this.mockMvc.perform(get("/v1/accounts/" + "111")).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + "111" + "\",\"balance\":500}"));

		// verify balance in account = 123 (accountToId) and it should be 2500
		this.mockMvc.perform(get("/v1/accounts/" + "123")).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + "123" + "\",\"balance\":2500}"));
	}

	@Test
	public void transferMoneyWhenAmountIsGreater() throws Exception {
		//create account=111 (accuntFromId)
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"111\",\"balance\":1000}")).andExpect(status().isCreated());

		
		// create account = 123 (accountToId)
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"123\",\"balance\":2000}")).andExpect(status().isCreated());
		
		// transact zero amount which should be considered as good request as per current implementation
		this.mockMvc
				.perform(post("/v1/accounts/transaction").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"111\",\"accountToId\":\"123\",\"amount\":1500}"))
				.andExpect(status().isBadRequest());
		
		// verify balance in account = 111 (accountFromId) and it should be 1000
		this.mockMvc.perform(get("/v1/accounts/" + "111")).andExpect(status().isOk())
		.andExpect(content().string("{\"accountId\":\"" + "111" + "\",\"balance\":1000}"));
		
		// verify balance in account = 123 (accountToId) and it should be 2000
		this.mockMvc.perform(get("/v1/accounts/" + "123")).andExpect(status().isOk())
		.andExpect(content().string("{\"accountId\":\"" + "123" + "\",\"balance\":2000}"));
	}

}
