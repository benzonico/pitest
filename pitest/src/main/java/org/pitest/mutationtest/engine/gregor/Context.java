/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */
package org.pitest.mutationtest.engine.gregor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.MutableList;
import org.pitest.functional.Option;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.blocks.BlockCounter;
import org.pitest.mutationtest.engine.gregor.blocks.ConcreteBlockCounter;

public class Context implements BlockCounter {

  private final Map<String, Integer>            mutatorIndexes                 = new HashMap<String, Integer>();

  private final ConcreteBlockCounter            blockCounter                   = new ConcreteBlockCounter();

  private int                                   lastLineNumber;

  private ClassInfo                             classInfo;
  private String                                sourceFile;

  private Option<MutationIdentifier>            target                         = Option
                                                                                   .none();
  private final FunctionalList<MutationDetails> mutations                      = new MutableList<MutationDetails>();
  private Location                              location;

  private final Set<String>                     mutationFindingDisabledReasons = new HashSet<String>();

  private void registerMutation(final MutationDetails details) {
    if (!isMutationFindingDisabled()) {
      this.mutations.add(details);
    }
  }

  private boolean isMutationFindingDisabled() {
    return !this.mutationFindingDisabledReasons.isEmpty();
  }

  public Option<MutationIdentifier> getTargetMutation() {
    return this.target;
  }

  private MutationIdentifier getNextMutationIdentifer(
      final MethodMutatorFactory factory, final String className) {
    final int index = getAndIncrementIndex(factory);
    return new MutationIdentifier(this.location, index,
        factory.getGloballyUniqueId());
  }

  private int getAndIncrementIndex(final MethodMutatorFactory factory) {
    Integer index = this.mutatorIndexes.get(factory.getGloballyUniqueId());
    if (index == null) {
      index = 0;
    }
    this.mutatorIndexes.put(factory.getGloballyUniqueId(), (index + 1));
    return index;

  }

  public Collection<MutationDetails> getCollectedMutations() {
    return this.mutations;
  }

  public void registerCurrentLine(final int line) {
    this.lastLineNumber = line;
  }

  public ClassInfo getClassInfo() {
    return this.classInfo;
  }

  private String getJavaClassName() {
    return this.classInfo.getName().replace("/", ".");
  }

  public String getFileName() {
    return this.sourceFile;
  }

  public int getLineNumber() {
    return this.lastLineNumber;
  }

  public MutationIdentifier registerMutation(
      final MethodMutatorFactory factory, final String description) {
    final MutationIdentifier newId = getNextMutationIdentifer(factory,
        getJavaClassName());
    final MutationDetails details = new MutationDetails(newId, getFileName(),
        description, this.lastLineNumber, this.blockCounter.getCurrentBlock(),
        this.blockCounter.isWithinFinallyBlock(), false);
    registerMutation(details);
    return newId;
  }

  public void setTargetMutation(final Option<MutationIdentifier> target) {
    this.target = target;
  }

  public List<MutationDetails> getMutationDetails(final MutationIdentifier id) {
    return this.mutations.filter(hasId(id));
  }

  private static F<MutationDetails, Boolean> hasId(final MutationIdentifier id) {
    return new F<MutationDetails, Boolean>() {
      public Boolean apply(final MutationDetails a) {
        return a.matchesId(id);
      }

    };
  }

  public void registerClass(final ClassInfo classInfo) {
    this.classInfo = classInfo;
  }

  public void registerSourceFile(final String source) {
    this.sourceFile = source;
  }

  public void registerMethod(final String name, final String descriptor) {
    this.location = Location.location(
        ClassName.fromString(this.classInfo.getName()),
        MethodName.fromString(name), descriptor);
    this.mutatorIndexes.clear();
  }

  public boolean shouldMutate(final MutationIdentifier newId) {
    return getTargetMutation().contains(idMatches(newId));
  }

  private static F<MutationIdentifier, Boolean> idMatches(
      final MutationIdentifier newId) {
    return new F<MutationIdentifier, Boolean>() {
      public Boolean apply(final MutationIdentifier a) {
        return a.matches(newId);
      }
    };
  }

  public void disableMutations(final String reason) {
    this.mutationFindingDisabledReasons.add(reason);
  }

  public void enableMutatations(final String reason) {
    this.mutationFindingDisabledReasons.remove(reason);
  }

  public void registerNewBlock() {
    this.blockCounter.registerNewBlock();
  }

  public void registerFinallyBlockStart() {
    this.blockCounter.registerFinallyBlockStart();
  }

  public void registerFinallyBlockEnd() {
    this.blockCounter.registerFinallyBlockEnd();
  }

}
