package com.playerstats;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlayerStatsFontType {
    BOLD("RuneScape bold"),
    REGULAR("RuneScape regular"),
    SMALL("RuneScape small"),
    ARIAL("Arial bold");

    private final String name;

    @Override
    public String toString()
    {
        return name;
    }
}
