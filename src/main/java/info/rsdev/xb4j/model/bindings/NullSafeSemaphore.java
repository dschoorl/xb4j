/* Copyright 2013 Red Star Development / Dave Schoorl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.rsdev.xb4j.model.bindings;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Provide a utility class that can be used as a placeholder for IModelAware instances, to facilitate nullsafe
 * locking and unlocking for bindings that are not yet added to a complete binding tree (meaning: they have not 
 * yet been directly or indirectly added to an {@link IModelAware} instance, such as {@link Root} or {@link ComplexType}).
 *  
 * @author Dave Schoorl
 */
public final class NullSafeSemaphore implements ISemaphore {
	
	public static final NullSafeSemaphore INSTANCE = new NullSafeSemaphore();
	
	private ReentrantLock lock = new ReentrantLock();

	private NullSafeSemaphore() {}

	@Override
    public void lock() {
    	this.lock.lock();
    }
    
	@Override
    public void unlock() {
    	this.lock.unlock();
    }
	
}
