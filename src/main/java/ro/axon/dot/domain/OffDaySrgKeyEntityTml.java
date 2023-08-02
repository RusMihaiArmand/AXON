package ro.axon.dot.domain;


import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
public abstract class OffDaySrgKeyEntityTml<T> {

    @SuppressWarnings("rawtypes")
    private static final PredicatesImpl PREDICATES = new PredicatesImpl();


    @SuppressWarnings("unchecked")
    public static <I, E extends OffDaySrgKeyEntityTml<I>> Predicates<I, E> predicates() {
        return PREDICATES;
    }

    @Override
    public String toString() {
        return String.format("%s#%s,v%s", entityShortName(), getId());
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
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (entityRefClass().isInstance(obj)) {
            @SuppressWarnings("rawtypes") final OffDaySrgKeyEntityTml other = (OffDaySrgKeyEntityTml) obj;
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
    protected abstract Class<? extends OffDaySrgKeyEntityTml<T>> entityRefClass();

    public interface Predicates<I, E extends OffDaySrgKeyEntityTml<I>> {

        Predicate<E> idIn(@Nonnull Collection<I> idsIncl);
    }

    private static class PredicatesImpl<T> implements Predicates<T, OffDaySrgKeyEntityTml<T>> {

        @Override
        public Predicate<OffDaySrgKeyEntityTml<T>> idIn(@Nonnull Collection<T> idsIncl) {
            return new IdInPredicate<>(idsIncl);
        }
    }

    private static class IdInPredicate<T> implements Predicate<OffDaySrgKeyEntityTml<T>> {
        private final Collection<T> idsIncl;

        IdInPredicate(final Collection<T> idsIncl) {
            this.idsIncl = idsIncl;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean test(final OffDaySrgKeyEntityTml<T> srgKeyEntity) {
            return idsIncl.contains(srgKeyEntity.getId());
        }
    }
}
