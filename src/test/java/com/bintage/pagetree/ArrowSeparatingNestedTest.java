package com.bintage.pagetree;

import org.junit.jupiter.api.IndicativeSentencesGeneration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@IndicativeSentencesGeneration(separator = "-> ")
public @interface ArrowSeparatingNestedTest {
}
