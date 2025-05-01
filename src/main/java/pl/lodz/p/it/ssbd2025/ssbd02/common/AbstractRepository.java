package pl.lodz.p.it.ssbd2025.ssbd02.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@NoRepositoryBean
public interface AbstractRepository<T> extends ListPagingAndSortingRepository<T, UUID>, CrudRepository<T, UUID> {}
