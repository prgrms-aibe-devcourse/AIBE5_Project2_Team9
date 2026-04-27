package com.pickkasso.pickkasso.user.cash.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CashChargeResponse {
    private Long historyId;
    private Integer newBalance;
}
