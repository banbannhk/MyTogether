package org.th.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PricePreference {
    LOW("သက်သာ"),
    MEDIUM("ပုံမှန်"),
    HIGH("တန်ဖိုးမြင့်");

    private final String labelMm;
}
