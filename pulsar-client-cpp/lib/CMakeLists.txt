#
# Copyright 2016 Yahoo Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

file(GLOB PULSAR_SOURCES *.cc lz4/*.c checksum/*.cc)

execute_process(COMMAND bash -c "cat ../pom.xml | grep -Po '(<?<version>)[^<]+' | sed 's/.*>//'  | head -1"
                OUTPUT_VARIABLE PULSAR_VERSION)
MESSAGE( STATUS "PULSAR VERSION:         " ${PULSAR_VERSION} )

set (CMAKE_CXX_FLAGS " ${CMAKE_CXX_FLAGS} -msse4.2 -mpclmul -D_PULSAR_VERSION_=\\\"${PULSAR_VERSION}\\\" ")

add_library(pulsarStatic STATIC ${PULSAR_SOURCES})
add_library(pulsarShared SHARED ${PULSAR_SOURCES})

set_target_properties(pulsarStatic PROPERTIES OUTPUT_NAME pulsar VERSION ${PULSAR_VERSION})
set_target_properties(pulsarShared PROPERTIES OUTPUT_NAME pulsar VERSION ${PULSAR_VERSION})

target_link_libraries(pulsarStatic ${COMMON_LIBS})
target_link_libraries(pulsarShared ${COMMON_LIBS})

add_subdirectory(auth)
