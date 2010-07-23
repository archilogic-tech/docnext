package jp.archilogic.documentmanager.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

@SuppressWarnings( "unchecked" )
public class GenericDao< T , PK extends Serializable > extends HibernateDaoSupport {
    private final Class< T > type =
            ( Class< T > ) ( ( ParameterizedType ) getClass().getGenericSuperclass() ).getActualTypeArguments()[ 0 ];

    public PK create( T o ) {
        return ( PK ) getHibernateTemplate().save( o );
    }

    public void delete( PK id ) {
        getHibernateTemplate().delete( findById( id ) );
    }

    public List< T > findAll() {
        return getHibernateTemplate().loadAll( type );
    }

    public List< T > findByExample( T exampleObject ) {
        return getHibernateTemplate().findByExample( exampleObject );
    }

    public T findById( PK id ) {
        return getHibernateTemplate().get( type , id );
    }

    @SuppressWarnings( "unused" )
    @Autowired
    private void setSessionFactoryOnSpring( SessionFactory sessionFactory ) {
        setSessionFactory( sessionFactory );
    }

    public void update( T o ) {
        getHibernateTemplate().update( o );
    }
}
