package org.hibernate.cfg;

import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.Target;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.SourceType;
import org.hibernate.cfg.annotations.HCANNHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

import javax.persistence.*;
import java.util.*;

/**
 * 跟官方的一样，但是这里不是用Treemap 使用LinkedHashmap 因为Treemap是按照key排序的，无法保证顺序
 * @author sufu
 * @date 2021/1/23
 */
public class PropertyContainer {
    private static final CoreMessageLogger LOG = Logger.getMessageLogger(CoreMessageLogger.class, PropertyContainer.class.getName());

    /**
     * The class for which this container is created.
     */
    private final XClass xClass;
    private final XClass entityAtStake;

    /**
     * Holds the AccessType indicated for use at the class/container-level for cases where persistent attribute
     * did not specify.
     */
    private final AccessType classLevelAccessType;

    private final LinkedHashMap<String, XProperty> persistentAttributeMap;

    PropertyContainer(XClass clazz, XClass entityAtStake, AccessType defaultClassLevelAccessType) {
        LOG.info("使用自定义的linkedHashmap存实体类的属性，保证顺序");
        this.xClass = clazz;
        this.entityAtStake = entityAtStake;

        if ( defaultClassLevelAccessType == AccessType.DEFAULT ) {
            // this is effectively what the old code did when AccessType.DEFAULT was passed in
            // to getProperties(AccessType) from AnnotationBinder and InheritanceState
            defaultClassLevelAccessType = AccessType.PROPERTY;
        }

        AccessType localClassLevelAccessType = determineLocalClassDefinedAccessStrategy();
        assert localClassLevelAccessType != null;

        this.classLevelAccessType = localClassLevelAccessType != AccessType.DEFAULT
                ? localClassLevelAccessType
                : defaultClassLevelAccessType;
        assert classLevelAccessType == AccessType.FIELD || classLevelAccessType == AccessType.PROPERTY;

        this.persistentAttributeMap = new LinkedHashMap<>();

        final List<XProperty> fields = xClass.getDeclaredProperties( AccessType.FIELD.getType() );
        final List<XProperty> getters = xClass.getDeclaredProperties( AccessType.PROPERTY.getType() );

        preFilter( fields, getters );

        final Map<String,XProperty> persistentAttributesFromGetters = new HashMap<>();

        collectPersistentAttributesUsingLocalAccessType(
                persistentAttributeMap,
                persistentAttributesFromGetters,
                fields,
                getters
        );
        collectPersistentAttributesUsingClassLevelAccessType(
                persistentAttributeMap,
                persistentAttributesFromGetters,
                fields,
                getters
        );
    }
    /**
     * 过滤一次获取的属性
     **/
    private void preFilter(List<XProperty> fields, List<XProperty> getters) {
        // 检查是否需要跳过该属性 通过检查是不是被 @Transient 这个注解注释了
        // 或者该属性的类是 InterceptFieldCallback 或FieldHandler 都移除
        Iterator<XProperty> propertyIterator = fields.iterator();
        while ( propertyIterator.hasNext() ) {
            final XProperty property = propertyIterator.next();
            if ( mustBeSkipped( property ) ) {
                propertyIterator.remove();
            }
        }

        propertyIterator = getters.iterator();
        while ( propertyIterator.hasNext() ) {
            final XProperty property = propertyIterator.next();
            if ( mustBeSkipped( property ) ) {
                propertyIterator.remove();
            }
        }
    }

    private void collectPersistentAttributesUsingLocalAccessType(
            LinkedHashMap<String, XProperty> persistentAttributeMap,
            Map<String,XProperty> persistentAttributesFromGetters,
            List<XProperty> fields,
            List<XProperty> getters) {

        // Check fields...
        Iterator<XProperty> propertyIterator = fields.iterator();
        while ( propertyIterator.hasNext() ) {
            final XProperty xProperty = propertyIterator.next();
            final Access localAccessAnnotation = xProperty.getAnnotation( Access.class );
            if ( localAccessAnnotation == null
                    || localAccessAnnotation.value() != javax.persistence.AccessType.FIELD ) {
                continue;
            }

            propertyIterator.remove();
            persistentAttributeMap.put( xProperty.getName(), xProperty );
        }

        // Check getters...
        propertyIterator = getters.iterator();
        while ( propertyIterator.hasNext() ) {
            final XProperty xProperty = propertyIterator.next();
            final Access localAccessAnnotation = xProperty.getAnnotation( Access.class );
            if ( localAccessAnnotation == null
                    || localAccessAnnotation.value() != javax.persistence.AccessType.PROPERTY ) {
                continue;
            }

            propertyIterator.remove();

            final String name = xProperty.getName();

            // HHH-10242 detect registration of the same property getter twice - eg boolean isId() + UUID getId()
            final XProperty previous = persistentAttributesFromGetters.get( name );
            if ( previous != null ) {
                throw new org.hibernate.boot.MappingException(
                        LOG.ambiguousPropertyMethods(
                                xClass.getName(),
                                HCANNHelper.annotatedElementSignature( previous ),
                                HCANNHelper.annotatedElementSignature( xProperty )
                        ),
                        new Origin( SourceType.ANNOTATION, xClass.getName() )
                );
            }

            persistentAttributeMap.put( name, xProperty );
            persistentAttributesFromGetters.put( name, xProperty );
        }
    }

    private void collectPersistentAttributesUsingClassLevelAccessType(
            LinkedHashMap<String, XProperty> persistentAttributeMap,
            Map<String,XProperty> persistentAttributesFromGetters,
            List<XProperty> fields,
            List<XProperty> getters) {
        if ( classLevelAccessType == AccessType.FIELD ) {
            for ( XProperty field : fields ) {
                if ( persistentAttributeMap.containsKey( field.getName() ) ) {
                    continue;
                }

                persistentAttributeMap.put( field.getName(), field );
            }
        }
        else {
            for ( XProperty getter : getters ) {
                final String name = getter.getName();

                // HHH-10242 detect registration of the same property getter twice - eg boolean isId() + UUID getId()
                final XProperty previous = persistentAttributesFromGetters.get( name );
                if ( previous != null ) {
                    throw new org.hibernate.boot.MappingException(
                            LOG.ambiguousPropertyMethods(
                                    xClass.getName(),
                                    HCANNHelper.annotatedElementSignature( previous ),
                                    HCANNHelper.annotatedElementSignature( getter )
                            ),
                            new Origin( SourceType.ANNOTATION, xClass.getName() )
                    );
                }

                if ( persistentAttributeMap.containsKey( name ) ) {
                    continue;
                }

                persistentAttributeMap.put( getter.getName(), getter );
                persistentAttributesFromGetters.put( name, getter );
            }
        }
    }

    public XClass getEntityAtStake() {
        return entityAtStake;
    }

    public XClass getDeclaringClass() {
        return xClass;
    }

    public AccessType getClassLevelAccessType() {
        return classLevelAccessType;
    }

    public Collection<XProperty> getProperties() {
        assertTypesAreResolvable();
        return Collections.unmodifiableCollection( persistentAttributeMap.values() );
    }

    private void assertTypesAreResolvable() {
        for ( XProperty xProperty : persistentAttributeMap.values() ) {
            if ( !xProperty.isTypeResolved() && !discoverTypeWithoutReflection( xProperty ) ) {
                String msg = "Property " + StringHelper.qualify( xClass.getName(), xProperty.getName() ) +
                        " has an unbound type and no explicit target entity. Resolve this Generic usage issue" +
                        " or set an explicit target attribute (eg @OneToMany(target=) or use an explicit @Type";
                throw new AnnotationException( msg );
            }
        }
    }

    private AccessType determineLocalClassDefinedAccessStrategy() {
        AccessType classDefinedAccessType;

        AccessType hibernateDefinedAccessType = AccessType.DEFAULT;
        AccessType jpaDefinedAccessType = AccessType.DEFAULT;

        org.hibernate.annotations.AccessType accessType = xClass.getAnnotation( org.hibernate.annotations.AccessType.class );
        if ( accessType != null ) {
            hibernateDefinedAccessType = AccessType.getAccessStrategy( accessType.value() );
        }

        Access access = xClass.getAnnotation( Access.class );
        if ( access != null ) {
            jpaDefinedAccessType = AccessType.getAccessStrategy( access.value() );
        }

        if ( hibernateDefinedAccessType != AccessType.DEFAULT
                && jpaDefinedAccessType != AccessType.DEFAULT
                && hibernateDefinedAccessType != jpaDefinedAccessType ) {
            throw new MappingException(
                    "@AccessType and @Access specified with contradicting values. Use of @Access only is recommended. "
            );
        }

        if ( hibernateDefinedAccessType != AccessType.DEFAULT ) {
            classDefinedAccessType = hibernateDefinedAccessType;
        }
        else {
            classDefinedAccessType = jpaDefinedAccessType;
        }
        return classDefinedAccessType;
    }

    private static boolean discoverTypeWithoutReflection(XProperty p) {
        if ( p.isAnnotationPresent( OneToOne.class ) && !p.getAnnotation( OneToOne.class )
                .targetEntity()
                .equals( void.class ) ) {
            return true;
        }
        else if ( p.isAnnotationPresent( OneToMany.class ) && !p.getAnnotation( OneToMany.class )
                .targetEntity()
                .equals( void.class ) ) {
            return true;
        }
        else if ( p.isAnnotationPresent( ManyToOne.class ) && !p.getAnnotation( ManyToOne.class )
                .targetEntity()
                .equals( void.class ) ) {
            return true;
        }
        else if ( p.isAnnotationPresent( ManyToMany.class ) && !p.getAnnotation( ManyToMany.class )
                .targetEntity()
                .equals( void.class ) ) {
            return true;
        }
        else if ( p.isAnnotationPresent( org.hibernate.annotations.Any.class ) ) {
            return true;
        }
        else if ( p.isAnnotationPresent( ManyToAny.class ) ) {
            if ( !p.isCollection() && !p.isArray() ) {
                throw new AnnotationException( "@ManyToAny used on a non collection non array property: " + p.getName() );
            }
            return true;
        }
        else if ( p.isAnnotationPresent( Type.class ) ) {
            return true;
        }
        else if ( p.isAnnotationPresent( Target.class ) ) {
            return true;
        }
        return false;
    }

    private static boolean mustBeSkipped(XProperty property) {
        //TODO make those hardcoded tests more portable (through the bytecode provider?)
        return property.isAnnotationPresent( Transient.class )
                || "net.sf.cglib.transform.impl.InterceptFieldCallback".equals( property.getType().getName() )
                || "org.hibernate.bytecode.internal.javassist.FieldHandler".equals( property.getType().getName() );
    }
}
