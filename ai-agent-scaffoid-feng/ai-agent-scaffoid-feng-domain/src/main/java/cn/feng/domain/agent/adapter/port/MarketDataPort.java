package cn.feng.domain.agent.adapter.port;

public interface MarketDataPort {

    String queryRealtimeMarketJson(String market, String code, int recentNewsSize, boolean includeMinute);

}
