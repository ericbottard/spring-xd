/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.dirt.server.options;

import javax.validation.constraints.NotNull;


/**
 * Holds options that are common to both admin and container servers, when used in distributed mode. Note that single
 * node has its own options class, because valid values are different.
 * 
 * @author Eric Bottard
 */
public abstract class CommonDistributedOptions extends CommonOptions {

	public static enum Analytics {
		// note: memory is NOT an option here
		redis;
	}

	// To be split in Transport & DataTransport. see XD-707
	public static enum Transport {
		rabbit, redis;
	}


	private Analytics analytics;

	private Transport transport;

	// Should be pushed down to AdminOptions but currently
	// can't b/c of the way container runtime info is persisted
	private Store store;

	@NotNull
	public Analytics getXD_ANALYTICS() {
		return analytics;
	}

	@NotNull
	public SingleNodeOptions.Store getXD_STORE() {
		return store;
	}

	@NotNull
	public Transport getXD_TRANSPORT() {
		return transport;
	}

	public void setXD_ANALYTICS(Analytics analytics) {
		this.analytics = analytics;
	}

	public void setXD_STORE(SingleNodeOptions.Store store) {
		this.store = store;
	}

	public void setXD_TRANSPORT(Transport transport) {
		this.transport = transport;
	}
}
