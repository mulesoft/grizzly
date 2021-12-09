package org.glassfish.grizzly.nio.transport;

import org.glassfish.grizzly.nio.SelectorRunner;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;

import java.io.IOException;

public class TCPNIOTransportForRedirect extends TCPNIOTransport {

    protected SelectorRunner[] selectorRunnersForRedirect;

    public TCPNIOTransportForRedirect(String name) {
        super(name);
    }

    @Override
    protected synchronized void startSelectorRunners() throws IOException {
        int selectorRunnersCount = getSelectorRunnersCount();
        selectorRunners = new SelectorRunner[selectorRunnersCount];
        selectorRunnersForRedirect = new SelectorRunner[selectorRunnersCount];


        for (int i = 0; i < selectorRunnersCount - 1; i++) {
            final SelectorRunner runner = SelectorRunner.create(this);
            runner.start();
            selectorRunners[i] = runner;
        }

        final SelectorRunner redirectRunner = SelectorRunner.create(this);
        redirectRunner.start();
        selectorRunnersForRedirect[0] = redirectRunner;
    }

    @Override
    protected synchronized void stopSelectorRunners() {
        if (selectorRunners == null) {
            return;
        }

        for (int i = 0; i < selectorRunners.length; i++) {
            SelectorRunner runner = selectorRunners[i];
            if (runner != null) {
                runner.stop();
                selectorRunners[i] = null;
            }
            SelectorRunner redirectRunner = selectorRunnersForRedirect[i];
            if (redirectRunner != null) {
                redirectRunner.stop();
                selectorRunnersForRedirect[i] = null;
            }
        }

        selectorRunners = null;
        selectorRunnersForRedirect = null;
    }

    @Override
    protected SelectorRunner[] getSelectorRunnersForRedirect() {
        return selectorRunnersForRedirect;
    }

}
