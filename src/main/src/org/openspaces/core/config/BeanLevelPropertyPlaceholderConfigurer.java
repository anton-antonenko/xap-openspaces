package org.openspaces.core.config;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.BeansException;
import org.openspaces.core.config.BeanLevelProperties;

import java.util.Properties;
import java.util.HashSet;

/**
 * @author kimchy
 */
public class BeanLevelPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements BeanNameAware, BeanFactoryAware {

    private BeanLevelProperties beanLevelProperties;

    public BeanLevelPropertyPlaceholderConfigurer(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
        setIgnoreUnresolvablePlaceholders(true);
        setSystemPropertiesMode(SYSTEM_PROPERTIES_MODE_NEVER);
        setOrder(2);
    }

    private String beanName;

    private BeanFactory beanFactory;

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
            throws BeansException {

        String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
        for (int i = 0; i < beanNames.length; i++) {
            // Check that we're not parsing our own bean definition,
            // to avoid failing on unresolvable placeholders in properties file locations.
            if (!(beanNames[i].equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory))) {
                BeanDefinitionVisitor visitor = new PlaceholderResolvingBeanDefinitionVisitor(beanLevelProperties.getMergedBeanProperties(beanNames[i]));
                BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(beanNames[i]);
                try {
                    visitor.visitBeanDefinition(bd);
                }
                catch (BeanDefinitionStoreException ex) {
                    throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanNames[i], ex.getMessage());
                }
            }
        }
    }

    /**
     * BeanDefinitionVisitor that resolves placeholders in String values,
     * delegating to the <code>parseStringValue</code> method of the
     * containing class.
     */
    private class PlaceholderResolvingBeanDefinitionVisitor extends BeanDefinitionVisitor {

        private final Properties props;

        public PlaceholderResolvingBeanDefinitionVisitor(Properties props) {
            this.props = props;
        }

        protected String resolveStringValue(String strVal) throws BeansException {
            return parseStringValue(strVal, this.props, new HashSet());
        }
    }
}
