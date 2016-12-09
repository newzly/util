/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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
package com.outworkers.util.testing

import org.scalatest.FlatSpec

class GeneratorsTest extends FlatSpec {

  it should "generate a sized list based on the given argument" in {
    val limit = 10
    assert(genList[String](limit).size == limit)
  }

  it should "generate a sized map based on the given size argument" in {
    val limit = 10

    assert(genMap[String](limit).size == limit)
  }

  it should "generate a sized map of known key and value types" in {
    val limit = 10
    assert(genMap[Int, Int](limit).size == limit)
  }

  it should "automatically derive valid samples" in {
    val sample = gen[User]
    info(sample.trace())
  }

  it should "automatically derive generator samples for complex case classes" in {
    val sample = gen[CollectionSample]
    Console.println(sample)
    //info(sample.trace())
  }
}
