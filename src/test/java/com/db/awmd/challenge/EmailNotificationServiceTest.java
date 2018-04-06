package com.db.awmd.challenge;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.EmailNotificationService;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest
public class EmailNotificationServiceTest {

	@MockBean
	private EmailNotificationService notificationService;

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
		MockitoAnnotations.initMocks(this);
		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	/**
	 * Positive case: both Notification service Called On Successful Transfer
	 * 
	 * @throws Exception
	 */
	@Test
	public void bothNotificationCalledOnSuccessfulTransfer() throws Exception {

		doNothing().when(notificationService).notifyAboutTransfer(isA(Account.class), isA(String.class));

		// create account=111 (accuntFromId)
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"111\",\"balance\":1000}")).andExpect(status().isCreated());

		// create account = 123 (accountToId)
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"123\",\"balance\":2000}")).andExpect(status().isCreated());

		// transact 500 from 111 to 123
		this.mockMvc
				.perform(post("/v1/accounts/transaction").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"111\",\"accountToId\":\"123\",\"amount\":500}"))
				.andExpect(status().isOk());

		// verify notification service is called twice - once for ech account
		verify(notificationService, times(2)).notifyAboutTransfer(isA(Account.class), isA(String.class));

		// verify balance in account = 111 (accountFromId) should be 500
		mockMvc.perform(get("/v1/accounts/" + "111")).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + "111" + "\",\"balance\":500}"));

		// verify balance in account = 123 (accountToId) should be 2500
		mockMvc.perform(get("/v1/accounts/" + "123")).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + "123" + "\",\"balance\":2500}"));
	}
}
