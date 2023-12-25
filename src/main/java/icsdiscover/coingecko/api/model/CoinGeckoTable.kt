package icsdiscover.coingecko.api.model

import java.util.Date

data class Roi(val times: Double, val currency: String, val percentage: Double)

data class CoinGeckoTable(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val current_price: Double,
    val market_cap: Any,
    val market_cap_rank: Int,
    val fully_diluted_valuation: Any,
    val total_volume: Any,
    val high_24h: Double,
    val low_24h: Double,
    val price_change_24h: Double,
    val price_change_percentage_24h: Double,
    val market_cap_change_24h: Any,
    val market_cap_change_percentage_24h: Double,
    val circulating_supply: Double,
    val total_supply: Double,
    val max_supply: Double,
    val ath: Double,
    val ath_change_percentage: Double,
    val ath_date: Date,
    val atl: Double,
    val atl_change_percentage: Double,
    val atl_date: Date,
    val roi: Roi,
    val last_updated: Date
)
