package jp.archilogic.documentmanager.converter;

public interface IEntityToDtoConverter< E , D > {
    D toDto( E entity );
}
