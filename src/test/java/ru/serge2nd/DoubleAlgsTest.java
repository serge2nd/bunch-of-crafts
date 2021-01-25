package ru.serge2nd;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.objectweb.asm.*;

import java.lang.invoke.MethodHandle;
import java.util.Random;

import static java.lang.Double.parseDouble;
import static java.lang.Double.toHexString;
import static java.lang.Float.parseFloat;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.match.AssertAllMatch.assertAllMatch;
import static ru.serge2nd.test.match.CommonMatch.equalTo;

@TestInstance(Lifecycle.PER_CLASS)
class DoubleAlgsTest implements NoInstanceTest<DoubleAlgs> {
    static final Class<?> ORIG = DoubleAlgs.class;
    static final Class<?> CLS = fitClassForTest();
    @SneakyThrows
    static Class<?> fitClassForTest() {
        ClassWriter out = new ClassWriter(COMPUTE_FRAMES);
        new ClassReader(ORIG.getName()).accept(new ClassVisitor(ASM5, out), SKIP_DEBUG | SKIP_FRAMES);
        byte[] bytes = out.toByteArray();

        return new ClassLoader(null) { @SneakyThrows
        public Class<?> findClass(String name) { return ORIG.getName().equals(name) ? defineClass(name, bytes, 0, bytes.length) : Class.forName(name); }
        }.findClass(ORIG.getName());
    }

    @Test void testSumAlgorithms() throws Throwable {
        MethodHandle mPlainSum = lookup().findStatic(CLS, "plainSum"      , methodType(float[].class, float[].class, float.class));
        MethodHandle mKahanSum = lookup().findStatic(CLS, "kahanSum"      , methodType(float[].class, float[].class, float.class));
        MethodHandle mNeumaSum = lookup().findStatic(CLS, "neumaierSum"   , methodType(float[].class, float[].class, float.class));
        MethodHandle mKleinSum = lookup().findStatic(CLS, "kleinSum"      , methodType(float[].class, float[].class, float.class));
        MethodHandle mItKBSum  = lookup().findStatic(CLS, "iterativeKBSum", methodType(float[].class, float[].class, float.class, int.class));

        Random rnd = new Random(79);
        double s = 0.0;
        float[] plainSum = new float[1];
        float[] kahanSum = new float[2];
        float[] neumaSum = new float[2];
        float[] kleinSum = new float[3];
        float[] itKB3Sum = new float[4];

        for (int i = 0; i < 50_000_000; i++) {
            float x = rnd.nextFloat(); s += x;
            mPlainSum.invoke(plainSum, x);
            mKahanSum.invoke(kahanSum, x);
            mNeumaSum.invoke(neumaSum, x);
            mKleinSum.invoke(kleinSum, x);
            mItKBSum.invoke(itKB3Sum, x, itKB3Sum.length - 1);
        }
        double exact1 = s;
        double exact2 = valueOf(itKB3Sum[0]).add(valueOf(itKB3Sum[1])).add(valueOf(itKB3Sum[2])).add(valueOf(itKB3Sum[3])).doubleValue();

        assertEach(() ->
        assertEquals(parseFloat ("0x1.0p24")         , plainSum[0]), () ->
        assertEquals(parseFloat ("0x1.7d77f6p24")    , kahanSum[0] + kahanSum[1]), () ->
        assertEquals(parseFloat ("0x1.7d7852p24")    , neumaSum[0] + neumaSum[1]), () ->
        assertEquals(parseDouble("0x1.7d77f52cep24") , (double)kleinSum[0] + kleinSum[1] + kleinSum[2]), () ->
        assertAllMatch(equalTo("0x1.7d77f52be48fp24"), toHexString(exact1), toHexString(exact2)));
    }

    static class ClassVisitor extends org.objectweb.asm.ClassVisitor {
        ClassVisitor(int api, org.objectweb.asm.ClassVisitor cv) { super(api, cv); }
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if ("plainSum".equals(name) || "neumaierSum".equals(name) || "kleinSum".equals(name)) {
                return new DoubleToFloatMethodVisitor(api, super.visitMethod(access, name, d2f(descriptor), signature, exceptions), DEFAULT_VI);
            } else if ("kahanSum".equals(name)) {
                return new DoubleToFloatMethodVisitor(api, super.visitMethod(access, name, d2f(descriptor), signature, exceptions), KH_SUM_VI);
            } else if ("iterativeKBSum".equals(name)) {
                return new DoubleToFloatMethodVisitor(api, super.visitMethod(access, name, d2f(descriptor), signature, exceptions), ITR_KB_SUM_VI);
            } else if ("compensationStep".equals(name)) {
                return new DoubleToFloatMethodVisitor(api, super.visitMethod(access, name, d2f(descriptor), signature, exceptions), CMPN_STEP_VI);
            }
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
        static final int[] DEFAULT_VI    = {0, 1};
        static final int[] KH_SUM_VI     = {0, 1, 1, 2, 2, 3, 3};
        static final int[] ITR_KB_SUM_VI = {0, 1, 1, 2, 3};
        static final int[] CMPN_STEP_VI  = {0, 1, 1, 2, 3, 3, 4, 4};
    }

    static class DoubleToFloatMethodVisitor extends MethodVisitor {
        final int[] varIds;
        DoubleToFloatMethodVisitor(int api, MethodVisitor mv, int[] varIds) { super(api, mv); this.varIds = varIds; }
        @Override
        public void visitInsn(int op)           { super.visitInsn(d2f(op)); }
        @Override
        public void visitVarInsn(int op, int i) { super.visitVarInsn(d2f(op), varIds[i]); }
        @Override
        public void visitIincInsn(int i, int x) { super.visitIincInsn(varIds[i], x); }
        @Override
        public void visitMethodInsn(int op, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(op, owner, name, d2f(descriptor), isInterface);
        }
    }

    static int d2f(int op) { return
        op == DLOAD   ? FLOAD :
        op == DSTORE  ? FSTORE :
        op == DALOAD  ? FALOAD :
        op == DASTORE ? FASTORE :
        op == DADD    ? FADD :
        op == DSUB    ? FSUB :
        op == DCMPL   ? FCMPL :
        op == DRETURN ? FRETURN : op;
    }
    static String d2f(String descriptor) { return descriptor.replace('D', 'F'); }
}