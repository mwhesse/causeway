/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.core.metamodel.runtimecontext.noruntime;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.query.Query;
import org.apache.isis.core.metamodel.adapter.DomainObjectServices;
import org.apache.isis.core.metamodel.adapter.DomainObjectServicesAbstract;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectDirtier;
import org.apache.isis.core.metamodel.adapter.ObjectDirtierAbstract;
import org.apache.isis.core.metamodel.adapter.ObjectPersistor;
import org.apache.isis.core.metamodel.adapter.ObjectPersistorAbstract;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.QuerySubmitterAbstract;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.ServicesProviderAbstract;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.adapter.map.AdapterMapAbstract;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.authentication.AuthenticationSessionProviderAbstract;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjector;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjectorAbstract;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAbstract;
import org.apache.isis.core.metamodel.spec.ObjectInstantiationException;
import org.apache.isis.core.metamodel.spec.ObjectInstantiator;
import org.apache.isis.core.metamodel.spec.ObjectInstantiatorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class RuntimeContextNoRuntime extends RuntimeContextAbstract {

	private DependencyInjector dependencyInjector;
    private AuthenticationSessionProviderAbstract authenticationSessionProvider;
    private AdapterMapAbstract adapterMap;
    private ObjectInstantiatorAbstract objectInstantiator;
    private ObjectDirtierAbstract objectDirtier;
    private ObjectPersistorAbstract objectPersistor;
    private DomainObjectServicesAbstract domainObjectServices;
    private QuerySubmitterAbstract querySubmitter;

    public RuntimeContextNoRuntime() {
        dependencyInjector = new DependencyInjectorAbstract() {
            
            /**
             * Unlike most of the methods in this implementation, does nothing (because
             * this will always be called, even in a no-runtime context).
             */
            @Override
            public void injectDependenciesInto(Object domainObject) {
                
            }
        };
        authenticationSessionProvider = new AuthenticationSessionProviderAbstract() {
            @Override
            public AuthenticationSession getAuthenticationSession() {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
        };
        adapterMap = new AdapterMapAbstract() {
            
            @Override
            public ObjectAdapter getAdapterFor(Object pojo) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public ObjectAdapter adapterFor(Object pojo, ObjectAdapter ownerAdapter, IdentifiedHolder identifiedHolder) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public ObjectAdapter adapterFor(Object domainObject) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
        };
        objectInstantiator = new ObjectInstantiatorAbstract() {
            
            @Override
            public Object instantiate(Class<?> cls) throws ObjectInstantiationException {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
        };
        objectDirtier = new ObjectDirtierAbstract() {
            
            @Override
            public void objectChanged(Object object) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public void objectChanged(ObjectAdapter adapter) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
        };
        objectPersistor = new ObjectPersistorAbstract() {
            
            @Override
            public void remove(ObjectAdapter adapter) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public void makePersistent(ObjectAdapter adapter) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
        };
        domainObjectServices = new DomainObjectServicesAbstract() {
            
            @Override
            public void warnUser(String message) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public void resolve(Object parent, Object field) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public void resolve(Object parent) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public void raiseError(String message) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public void informUser(String message) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public List<String> getPropertyNames() {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public String getProperty(String name) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public boolean flush() {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public ObjectAdapter createTransientInstance(ObjectSpecification spec) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public void commit() {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
        };
        querySubmitter = new QuerySubmitterAbstract() {
            
            @Override
            public <T> ObjectAdapter firstMatchingQuery(Query<T> query) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
            
            @Override
            public <T> List<ObjectAdapter> allMatchingQuery(Query<T> query) {
                throw new UnsupportedOperationException(
                "Not supported by this implementation of RuntimeContext");
            }
        };
	}


	/////////////////////////////////////////////
	// Components
	/////////////////////////////////////////////

    @Override
    public AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }
    
    @Override
    public AdapterMap getAdapterMap() {
        return adapterMap;
    }

    @Override
    public ObjectInstantiator getObjectInstantiator() {
        return objectInstantiator;
    }
    
    @Override
    public ObjectDirtier getObjectDirtier() {
        return objectDirtier;
    }

    @Override
    public ObjectPersistor getObjectPersistor() {
        return objectPersistor;
    }

    @Override
    public DomainObjectServices getDomainObjectServices() {
        return domainObjectServices;
    }

    @Override
    public QuerySubmitter getQuerySubmitter() {
        return querySubmitter;
    }

    @Override
    public DependencyInjector getDependencyInjector() {
        return dependencyInjector;
    }
    
	
	/////////////////////////////////////////////
	// allInstances, allMatching*
	/////////////////////////////////////////////

	public List<ObjectAdapter> allInstances(ObjectSpecification noSpec) {
		throw new UnsupportedOperationException(
		"Not supported by this implementation of RuntimeContext");
	}

	
	/////////////////////////////////////////////
	// getServices, injectDependenciesInto
	/////////////////////////////////////////////

	@Override
    public ServicesProvider getServicesProvider() {
	    return new ServicesProviderAbstract() {
	        /**
	         * Just returns an empty array.
	         */
	        @Override
            public List<ObjectAdapter> getServices() {
	            return Collections.emptyList();
	        }
	    };
	}

}
