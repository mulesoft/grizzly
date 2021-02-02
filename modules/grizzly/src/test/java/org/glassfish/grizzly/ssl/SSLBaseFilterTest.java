package org.glassfish.grizzly.ssl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.InvokeAction;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.filterchain.StopAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SSLBaseFilterTest {

	private boolean remainsOutput;
	private boolean isClosed;
	private int expectedNextAction;

	public SSLBaseFilterTest(boolean remainsOutput, boolean isClosed, int expectedNextAction) {
		this.remainsOutput = remainsOutput;
		this.isClosed = isClosed;
		this.expectedNextAction = expectedNextAction;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> parameters() {
		return Arrays.asList(new Object[][] { { true, true, InvokeAction.TYPE }, { true, false, InvokeAction.TYPE },
				{ false, true, StopAction.TYPE }, { false, false, InvokeAction.TYPE } });
	}

	@Test
	public void testExpectedNextAction() throws Exception {
		// The filter chain should be stopped only if the connection is closed
		// and there is no output to be consumed.
		Buffer input = mock(Buffer.class);
		Buffer output = mock(Buffer.class);
		when(output.hasRemaining()).thenReturn(remainsOutput);
		verifyAction(input, output, isClosed, expectedNextAction);
	}

	@Test
	public void nullOutputAlwaysStopsFilter() throws Exception {
		Buffer input = mock(Buffer.class);
		verifyAction(input, null, isClosed, 1);
	}

	private void verifyAction(Buffer input, Buffer output, boolean isClosed, int expectedNextActionType) {
		SSLBaseFilter filter = new SSLBaseFilter();
		FilterChainContext filterContextChain = new FilterChainContext();
		SSLConnectionContext sslConnectionContext = new SSLConnectionContext(null);
		NextAction action = filter.resolveNextAction(filterContextChain, sslConnectionContext, input, output, isClosed);
		assertEquals(action.type(), expectedNextActionType);
	}
}
