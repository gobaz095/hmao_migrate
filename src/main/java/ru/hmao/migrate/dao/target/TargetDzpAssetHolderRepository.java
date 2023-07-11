package ru.hmao.migrate.dao.target;

import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpAssetHolder;

public interface TargetDzpAssetHolderRepository extends CrudRepository<TargetDzpAssetHolder, Long>, WithInsert<TargetDzpAssetHolder> {

}
