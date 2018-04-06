package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UserTransaction {

	@NotNull
	@NotEmpty
	private final String accountFromId;

	@NotNull
	@NotEmpty
	private final String accountToId;

	@NotNull
	@Min(value = 0, message = "Requested transaction amount cant be nagative")
	private BigDecimal amount;

	@JsonCreator
	public UserTransaction(@JsonProperty("accountFromId") String accountFrom, @JsonProperty("accountToId") String accountTo,
			@JsonProperty("amount") BigDecimal amount) {
		this.accountFromId = accountFrom;
		this.accountToId = accountTo;
		this.amount = amount;
	}
}
