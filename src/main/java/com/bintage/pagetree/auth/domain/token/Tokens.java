package com.bintage.pagetree.auth.domain.token;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
@SecondaryPort
public interface Tokens {

    Token save(Token token);

    Optional<Token> findById(Token.TokenId id);

    void updateStatus(Token token);

}
