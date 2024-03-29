/**
 * Copyright (C) 2011 Google Inc.
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
 *
 */

package com.wideplay.guicer.stat.testservices;

import com.wideplay.guicer.stat.Stat;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This subclass of {@link DummyService} exists to illustrate how stats are
 * published (or not published) within a class hierarchy.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public class ChildDummyService extends DummyService {

  public static final String NUMBER_OF_CHILD_CALLS = "number-of-child-calls";

  private final AtomicInteger childCalls = new AtomicInteger();

  @Override public void call() {
    super.call();
    childCalls.incrementAndGet();
  }

  @Stat(NUMBER_OF_CHILD_CALLS)
  public AtomicInteger getChildCalls() {
    return childCalls;
  }
}
