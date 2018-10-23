package com.reactnativenavigation.params.parsers;

import android.os.Bundle;

import com.reactnativenavigation.params.NavigationParams;
import com.reactnativenavigation.params.OverlayParams;

public class OverlayParamsParser extends Parser {

    public OverlayParams parse(Bundle bundle) {
        final OverlayParams result = new OverlayParams();
        result.screenInstanceId = bundle.getString("screen");
        result.navigationParams = new NavigationParams(bundle.getBundle("navigationParams"));
        result.autoDismissTimerSec = bundle.containsKey("autoDismissTimerSec")
                ? bundle.getInt("autoDismissTimerSec")
                : null;
        result.position = OverlayParams.Position.fromString(bundle.getString("position", "top"));
        return result;
    }
}
