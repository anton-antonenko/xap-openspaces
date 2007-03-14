package org.openspaces.core.config;

import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class ViewQueryBeanDefinitionParser extends SQLQueryBeanDefinitionParser {

    protected Class<ViewQueryFactoryBean> getBeanClass(Element element) {
        return ViewQueryFactoryBean.class;
    }
}