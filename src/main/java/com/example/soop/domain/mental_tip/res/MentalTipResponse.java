package com.example.soop.domain.mental_tip.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MentalTipResponse {
    private List<String> tips;
}
