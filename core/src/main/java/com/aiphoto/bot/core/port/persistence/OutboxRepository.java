package com.aiphoto.bot.core.port.persistence;

import com.aiphoto.bot.core.domain.OutboxEvent;

public interface OutboxRepository {

    OutboxEvent save(OutboxEvent event);
}
