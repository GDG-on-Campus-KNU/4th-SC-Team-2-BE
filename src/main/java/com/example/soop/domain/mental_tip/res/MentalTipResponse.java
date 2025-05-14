package com.example.soop.domain.mental_tip.res;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MentalTipResponse {
    private List<String> tips;
}