package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import com.gigaspaces.client.ReadTakeByIdResult;
import com.gigaspaces.client.ReadByIdsException.ReadByIdResult;

/**
 * Thrown when readByIds operation fails.
 *
 * <p>Thrown on:
 * <ul>
 * <li>Partial and complete failure.
 * <li>Cluster/single space topologies. 
 * </ul>
 *
 * <p>The exception contains an array of IReadByIdResult objects where each result in the array contains
 * either a read object or an exception upon failure. The result array index corresponds to the ID index in
 * the operation's supplied IDs array.
 * 
 * @author idan
 * @since 7.1.1
 *
 */
public class ReadByIdsException extends InvalidDataAccessResourceUsageException {

    private static final long serialVersionUID = 1L;
    private ExceptionTranslator _exceptionTranslator;
    private ReadByIdResult[] _results;
    
    public ReadByIdsException(com.gigaspaces.client.ReadByIdsException cause, ExceptionTranslator exceptionTranslator) {
        super(cause.getMessage(), cause);
        _exceptionTranslator = exceptionTranslator;
        _results = new ReadByIdResult[cause.getResults().length];
        for (int i = 0; i < _results.length; i++) {
            _results[i] = new TranslatedReadByIdResult(cause.getResults()[i]);
        }
    }

    /**
     * Returns the results contained in the exception.
     * @return An array of IReadByIdResult objects.
     */
    public ReadByIdResult[] getResults() {
        return _results;
    }
        
    private class TranslatedReadByIdResult implements ReadByIdResult {

        private ReadTakeByIdResult _result;
        private ReadByIdResultType _resultType;
        
        public TranslatedReadByIdResult(ReadTakeByIdResult result) {
            _result = result;
            if (_result.isError()) {
                _resultType = ReadByIdResultType.ERROR;                
            } else {
                _resultType = (_result.getObject() == null)? ReadByIdResultType.NOT_FOUND : ReadByIdResultType.OBJECT;
            }
        }
        
        public Throwable getError() {
            if (_result.getError() == null) {
                return null;
            }
            return _exceptionTranslator.translate(_result.getError());
        }

        public Object getId() {
            return _result.getId();
        }

        public Object getObject() {
            return _result.getObject();
        }

        public boolean isError() {
            return _result.isError();
        }
        
        public ReadByIdResultType getResultType() {
            return _resultType;
        }
    }
    
    
}
