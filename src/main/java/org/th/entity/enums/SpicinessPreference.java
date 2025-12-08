package org.th.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpicinessPreference {
    MILD("အစပ်လျှော့"),
    MEDIUM("အစပ်သင့်"),
    SPICY("အစပ်");

    private final String labelMm;
}
