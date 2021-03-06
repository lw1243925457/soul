/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.soul.register.client.http;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.dromara.soul.register.client.api.SoulClientRegisterRepository;
import org.dromara.soul.register.client.http.utils.RegisterUtils;
import org.dromara.soul.register.common.config.SoulRegisterCenterConfig;
import org.dromara.soul.register.common.dto.MetaDataRegisterDTO;
import org.dromara.soul.register.common.enums.RegisterTypeEnum;
import org.dromara.soul.spi.Join;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Http client register repository.
 *
 * @author xiaoyu
 */
@Slf4j
@Join
public class HttpClientRegisterRepository implements SoulClientRegisterRepository {

    private List<String> serverList;
    
    private Gson gson = new Gson();

    private Map<String, String> turn = new HashMap<>();

    @Override
    public void init(final SoulRegisterCenterConfig config) {
        this.serverList = Lists.newArrayList(Splitter.on(",").split(config.getServerLists()));
        initTurn();
    }

    protected void initTurn() {
        turn.put(RegisterTypeEnum.DUBBO.getName(), "/soul-client/dubbo-register");
        turn.put(RegisterTypeEnum.GRPC.getName(), "/soul-client/grpc-register");
        turn.put(RegisterTypeEnum.HTTP.getName(), "/soul-client/springmvc-register");
        turn.put(RegisterTypeEnum.SOFA.getName(), "/soul-client/sofa-register");
        turn.put(RegisterTypeEnum.SPRING_CLOUD.getName(), "/soul-client/springcloud-register");
        turn.put(RegisterTypeEnum.TARS.getName(), "/soul-client/tars-register");
    }

    @Override
    public void persistInterface(final MetaDataRegisterDTO metadata) {
        String rpcType = metadata.getRpcType();
        for (String server : serverList) {
            try {
                RegisterUtils.doRegister(gson.toJson(metadata), server + turn.get(rpcType), rpcType);
                return;
            } catch (Exception e) {
                log.error("register admin url :{} is fail, will retry", server);
            }
        }
    }
}
