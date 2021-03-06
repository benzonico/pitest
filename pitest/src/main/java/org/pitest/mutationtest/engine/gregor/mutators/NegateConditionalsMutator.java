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
package org.pitest.mutationtest.engine.gregor.mutators;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractJumpMutator;
import org.pitest.mutationtest.engine.gregor.Context;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

public enum NegateConditionalsMutator implements MethodMutatorFactory {

  NEGATE_CONDITIONALS_MUTATOR;

  public MethodVisitor create(final Context context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ConditionalMethodVisitor(this, context, methodVisitor);
  }

  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  public String getName() {
    return name();
  }

}

class ConditionalMethodVisitor extends AbstractJumpMutator {

  private static final String                     DESCRIPTION = "negated conditional";
  private final static Map<Integer, Substitution> mutations   = new HashMap<Integer, Substitution>();

  static {
    mutations.put(Opcodes.IFEQ, new Substitution(Opcodes.IFNE, DESCRIPTION));
    mutations.put(Opcodes.IFNE, new Substitution(Opcodes.IFEQ, DESCRIPTION));
    mutations.put(Opcodes.IFLE, new Substitution(Opcodes.IFGT, DESCRIPTION));
    mutations.put(Opcodes.IFGE, new Substitution(Opcodes.IFLT, DESCRIPTION));
    mutations.put(Opcodes.IFGT, new Substitution(Opcodes.IFLE, DESCRIPTION));
    mutations.put(Opcodes.IFLT, new Substitution(Opcodes.IFGE, DESCRIPTION));
    mutations.put(Opcodes.IFNULL, new Substitution(Opcodes.IFNONNULL,
        DESCRIPTION));
    mutations.put(Opcodes.IFNONNULL, new Substitution(Opcodes.IFNULL,
        DESCRIPTION));
    mutations.put(Opcodes.IF_ICMPNE, new Substitution(Opcodes.IF_ICMPEQ,
        DESCRIPTION));
    mutations.put(Opcodes.IF_ICMPEQ, new Substitution(Opcodes.IF_ICMPNE,
        DESCRIPTION));
    mutations.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPGT,
        DESCRIPTION));
    mutations.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPLT,
        DESCRIPTION));
    mutations.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPLE,
        DESCRIPTION));
    mutations.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPGE,
        DESCRIPTION));
    mutations.put(Opcodes.IF_ACMPEQ, new Substitution(Opcodes.IF_ACMPNE,
        DESCRIPTION));
    mutations.put(Opcodes.IF_ACMPNE, new Substitution(Opcodes.IF_ACMPEQ,
        DESCRIPTION));
  }

  public ConditionalMethodVisitor(final MethodMutatorFactory factory,
      final Context context, final MethodVisitor delegateMethodVisitor) {
    super(factory, context, delegateMethodVisitor);
  }

  @Override
  protected Map<Integer, Substitution> getMutations() {
    return mutations;
  }

}
