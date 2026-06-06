package com.nvs.watchlist.dto.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistResponse implements Serializable {
    public Long id;
    public String stockSymbol;
    public int position;
}