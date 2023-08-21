package ro.axon.dot.domain;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@MappedSuperclass
public abstract class SrgKeyEntityTml<T> {

    @SuppressWarnings("rawtypes")
    private static final PredicatesImpl PREDICATES = new PredicatesImpl();

    @SuppressWarnings("unused")
    @Version
    @Column(name = "V", nullable = false)
    private Long v;

    @SuppressWarnings("unchecked")
    public static <I, E extends SrgKeyEntityTml<I>> Predicates<I, E> predicates() {
        return PREDICATES;
    }

    @Override
    public String toString() {
        return String.format("%s#%s,v%s", entityShortName(), getId(), Optional.<Object>ofNullable(v).orElse("NA"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /**
     * @return the id
     */
    public abstract T getId();

    /**
     * @return the v
     */
    public Long getV() {
        return v;
    }

    public void setV(Long v) {
        this.v = v;
    }

    /**
     * @return {@code true} if this entity is a persisted one, {@code false} otherwise
     */
    public boolean persisted() {
        return Objects.nonNull(getV());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (entityRefClass().isInstance(obj)) {
            @SuppressWarnings("rawtypes") final SrgKeyEntityTml other = (SrgKeyEntityTml) obj;
            final T thisId = getId();
            return Objects.nonNull(thisId) && Objects.equals(thisId, other.getId());
        } else {
            return false;
        }
    }

    /**
     * @return short name of the entity to use in {@link #toString()}
     */
    @SuppressWarnings("WeakerAccess")
    protected String entityShortName() {
        return entityRefClass().getSimpleName();
    }

    /**
     * @return class of the entity
     */
    protected abstract Class<? extends SrgKeyEntityTml<T>> entityRefClass();

    public interface Predicates<I, E extends SrgKeyEntityTml<I>> {

        Predicate<E> idIn(@Nonnull Collection<I> idsIncl);
    }

    private static class PredicatesImpl<T> implements Predicates<T, SrgKeyEntityTml<T>> {

        @Override
        public Predicate<SrgKeyEntityTml<T>> idIn(@Nonnull Collection<T> idsIncl) {
            return new IdInPredicate<>(idsIncl);
        }
    }

    private static class IdInPredicate<T> implements Predicate<SrgKeyEntityTml<T>> {
        private final Collection<T> idsIncl;

        IdInPredicate(final Collection<T> idsIncl) {
            this.idsIncl = idsIncl;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean test(final SrgKeyEntityTml<T> srgKeyEntity) {
            return idsIncl.contains(srgKeyEntity.getId());
        }
    }
}
