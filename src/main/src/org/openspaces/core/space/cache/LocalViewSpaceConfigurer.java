/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.core.space.cache;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A simple configurer helper to create {@link IJSpace} local view. The configurer wraps
 * {@link LocalViewSpaceFactoryBean} and providing a simpler means
 * to configure it using code.
 *
 * <p>An example of using it:
 * <pre>
 * UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space").schema("persistent")
 *          .noWriteLeaseMode(true).lookupGroups(new String[] {"kimchy"});
 * IJSpace space = urlSpaceConfigurer.space();
 *
 * LocalViewSpaceConfigurer localViewConfigurer = new LocalViewSpaceConfigurer(space)
 *           .addView(new View(SimpleMessage.class, "processed = true"));
 * IJSpace localView = localViewConfigurer.localView();
 * ...
 * localViewConfigurer.destroy();
 * urlSpaceConfigurer.destroy(); // optional
 * </pre>
 *
 * @author kimchy
 */
public class LocalViewSpaceConfigurer {

    private LocalViewSpaceFactoryBean localViewSpaceFactoryBean;

    private IJSpace space;

    private Properties properties = new Properties();

    private List<View<?>> localViews = new ArrayList<View<?>>();

    public LocalViewSpaceConfigurer(IJSpace space) {
        localViewSpaceFactoryBean = new LocalViewSpaceFactoryBean();
        localViewSpaceFactoryBean.setSpace(space);
    }

    /**
     * @see LocalViewSpaceFactoryBean#setProperties(java.util.Properties)
     */
    public LocalViewSpaceConfigurer addProperty(String name, String value) {
        properties.setProperty(name, value);
        return this;
    }

    public LocalViewSpaceConfigurer addView(View view) {
        localViews.add(view);
        return this;
    }

    public IJSpace localView() {
        if (space == null) {
            localViewSpaceFactoryBean.setProperties(properties);
            localViewSpaceFactoryBean.setLocalViews(localViews);
            localViewSpaceFactoryBean.afterPropertiesSet();
            space = (IJSpace) localViewSpaceFactoryBean.getObject();
        }
        return this.space;
    }

    public void destroy() {
        localViewSpaceFactoryBean.destroy();
    }
}