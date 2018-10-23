package com.reactnativenavigation.views.Overlay;

import com.reactnativenavigation.NavigationApplication;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class OverlaysQueue implements Overlay.SlidingListener{

    private static final int SHORT_SUSTAIN_DURATION = 500;

    protected Timer autoDismissTimer = null;
    protected boolean pendingHide;
    protected Queue<Overlay> queue = new LinkedList<>();

    public void add(final Overlay overlay) {
        NavigationApplication.instance.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                queue.add(overlay);
                if (queue.size() == 1) {
                    dispatchNextOverlay();
                }
                else {
                    Overlay currentOverlay = queue.peek();
                    if (currentOverlay != null && currentOverlay.isVisible()) {
                        if (autoDismissTimer != null) {
                            autoDismissTimer.cancel();
                            autoDismissTimer = null;
                        }
                        currentOverlay.hide();
                    }
                }
            }
        });
    }

    public void remove() {
        NavigationApplication.instance.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Overlay currentOverlay = queue.peek();

                if(currentOverlay == null) {
                    return;
                }
                
                if (currentOverlay.isShowing()) {
                    pendingHide = true;
                }
                else if (currentOverlay.isVisible()) {
                    cancelTimer();
                    currentOverlay.hide();
                }
            }
        });
    }

    @Override
    public void onOverlayShown() {
        Integer autoDismissTimerSec = queue.peek() == null ? null : queue.peek().getAutoDismissTimerSec();

        if (autoDismissTimerSec != null || pendingHide || queue.size() > 1) {
            int autoDismissDuration = autoDismissTimerSec != null
                    ? autoDismissTimerSec * 1000
                    : SHORT_SUSTAIN_DURATION;
            pendingHide = false;

            autoDismissTimer = new Timer();
            autoDismissTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    NavigationApplication.instance.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if(queue.peek() != null) {
                                queue.peek().hide();
                            }
                        }
                    });
                }
            }, autoDismissDuration);
        }
    }

    @Override
    public void onOverlayGone() {
        queue.poll();
        dispatchNextOverlay();
    }

    public void destroy() {
        Overlay currentOverlay = queue.poll();
        while (!queue.isEmpty()) {
            queue.poll();
        }

        if (currentOverlay != null) {
            cancelTimer();
            currentOverlay.setSlidingListener(null);
            currentOverlay.destroy();
        }
    }

    protected void dispatchNextOverlay() {
        NavigationApplication.instance.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                final Overlay nextOverlay = queue.peek();
                if (nextOverlay != null) {
                    nextOverlay.setSlidingListener(OverlaysQueue.this);
                    nextOverlay.show();
                }
            }
        });
    }

    protected void cancelTimer() {
        if (autoDismissTimer != null) {
            autoDismissTimer.cancel();
            autoDismissTimer = null;
        }
    }
}
