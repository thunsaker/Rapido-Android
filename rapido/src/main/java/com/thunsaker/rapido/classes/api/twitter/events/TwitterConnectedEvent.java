package com.thunsaker.rapido.classes.api.twitter.events;

import com.thunsaker.android.common.BaseEvent;

public class TwitterConnectedEvent extends BaseEvent {
    public TwitterConnectedEvent(Boolean result, String resultMessage) {
        super(result, resultMessage);
    }
}
