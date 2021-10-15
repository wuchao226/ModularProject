package com.wuc.aroutet_compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wuc.arouter_annotation.Parameter;
import com.wuc.aroutet_compiler.utils.ProcessorConfig;
import com.wuc.aroutet_compiler.utils.ProcessorUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @author : wuchao5
 * @date : 2020/11/21 17:51
 * @desciption : 注解处理器
 * AutoService注解：就这么一标记，annotationProcessor  project()应用一下,编译时就能自动执行该类了。
 * AutoService注解 代替 @Retention(RetentionPolicy.CLASS)和 @Target(ElementType.TYPE)
 *
 * SupportedSourceVersion注解:声明我们所支持的jdk版本
 *
 * SupportedAnnotationTypes:声明该注解处理器想要处理那些注解
 */

// AutoService则是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)//启用 AutoService 服务

// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)

// 允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({ ProcessorConfig.PARAMETER_PACKAGE })
public class ParameterProcessor extends AbstractProcessor {

  // 操作Element的工具类（类，函数，属性，其实都是Element）
  private Elements elementTool;
  // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
  private Types typeTool;
  // Message用来打印 日志相关信息
  private Messager messager;
  // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
  private Filer filer;

  // 临时map存储，用来存放被@Parameter注解的属性集合，生成类文件时遍历
  // key:类节点, value:被@Parameter注解的属性集合
  private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

  /**
   * init()方法会被注解处理工具调用，并输入ProcessingEnviroment参数。
   * ProcessingEnviroment提供很多有用的工具类Elements, Types 和 Filer
   *
   * @param processingEnv 提供给 processor 用来访问工具框架的环境
   */
  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    elementTool = processingEnv.getElementUtils();
    typeTool = processingEnv.getTypeUtils();
    messager = processingEnv.getMessager();
    filer = processingEnv.getFiler();

  }

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // 由于返回了 false 就不需要一下代码了
    /*if (set.isEmpty()) {
        messager.printMessage(Diagnostic.Kind.NOTE, "并没有发现 被@ARouter注解的地方呀");
        return false; // 没有机会处理
     }*/

    // 扫描的时候，看那些地方使用到了@Parameter注解
    if (!ProcessorUtils.isEmpty(annotations)) {
      // 获取所有被 @Parameter 注解的 元素（属性）集合
      Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Parameter.class);
      if (!ProcessorUtils.isEmpty(elements)) {
        // TODO　给仓库 存储相关信息
        for (Element element : elements) {// element == name， sex,  age
          // 字段节点的上一个节点 类节点==Key
          // 注解在属性的上面，属性节点父节点 是 类节点
          TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
          // enclosingElement == PersonalMainActivity == key
          if (tempParameterMap.containsKey(enclosingElement)) {
            tempParameterMap.get(enclosingElement).add(element);
          } else {// 没有key PersonalMainActivity
            List<Element> fields = new ArrayList<>();
            fields.add(element);
            tempParameterMap.put(enclosingElement, fields);// 加入缓存
          }
        }//TODO end for

        // TODO 生成类文件
        // 判断是否有需要生成的类文件
        if (ProcessorUtils.isEmpty(tempParameterMap)) {
          return true;
        }
        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
        TypeElement parameterType = elementTool.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER_GET);

        // 生成方法
        // Object targetParameter
        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, ProcessorConfig.PARAMETER_NAME).build();

        // 循环遍历 缓存tempParameterMap
        // 可能很多地方都使用了 @Parameter注解，那么就需要去遍历 仓库
        for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
          // key：   PersonalMainActivity
          // value： [name,sex,age]
          TypeElement typeElement = entry.getKey();

          // 非Activity直接报错
          // 如果类名的类型和Activity类型不匹配
          if (!typeTool.isSubtype(typeElement.asType(), activityType.asType())) {
            throw new RuntimeException("@Parameter注解目前仅限用于Activity类之上");
          }

          // 是Activity
          // 获取类名 == PersonalMainActivity
          ClassName className = ClassName.get(typeElement);

          // 方法生成成功
          ParameterFactory factory = new ParameterFactory.Builder(parameterSpec)
              .setClassName(className)
              .setMessager(messager)
              .setElementUtils(elementTool)
              .setTypeUtils(typeTool)
              .build();
          // PersonalMainActivity t = (PersonalMainActivity) targetParameter;
          factory.addFirstStatement();

          // 难点 多行
          for (Element element : entry.getValue()) {
            factory.buildStatement(element);
          }
          // 最终生成的类文件名（类名$$Parameter） 例如：PersonalMainActivity$$Parameter
          String finalClassName = typeElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
          messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取参数类文件：" +
              className.packageName() + "." + finalClassName);

          // 开始生成文件，例如：PersonalMainActivity$$Parameter
          try {
            JavaFile.builder(className.packageName(),// 包名
                TypeSpec.classBuilder(finalClassName) // 类名
                    .addSuperinterface(ClassName.get(parameterType)) //  implements ParameterGet 实现ParameterLoad接口
                    .addModifiers(Modifier.PUBLIC) // public修饰符
                    .addMethod(factory.build()) // 方法的构建（方法参数 + 方法体）
                    .build())// 类构建完成
                .build()// JavaFile构建完成
                .writeTo(filer);// 文件生成器开始生成类文件
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return false;
  }
}
