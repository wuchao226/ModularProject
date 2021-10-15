package com.wuc.aroutet_compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.wuc.arouter_annotation.ARouter;
import com.wuc.arouter_annotation.bean.RouterBean;
import com.wuc.aroutet_compiler.utils.ProcessorConfig;
import com.wuc.aroutet_compiler.utils.ProcessorUtils;
import java.io.IOException;
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
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @author : wuchao5
 * @date : 2020/11/16 16:51
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

// 允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({ ProcessorConfig.AROUTER_PACKAGE })

// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)

// 注解处理器接收的参数
@SupportedOptions({ ProcessorConfig.OPTIONS, ProcessorConfig.APT_PACKAGE })
public class ArouterProcessor extends AbstractProcessor {

  // 操作Element的工具类（类，函数，属性，其实都是Element）
  private Elements elementTool;
  // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
  private Types typeTool;
  // Message用来打印 日志相关信息
  private Messager messager;
  // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
  private Filer filer;
  // 各个模块传递过来的模块名 例如：app order personal
  private String options;
  // 各个模块传递过来的目录 用于统一存放 apt生成的文件
  private String aptPackage;

  // 仓库一 Path  缓存一
  // Map<"personal", List<RouterBean>>
  private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>();

  // 仓库二 Group 缓存二
  // Map<"personal", "ARouter$$Path$$personal.class">
  private Map<String, String> mAllGroupMap = new HashMap<>();

  /**
   * init()方法会被注解处理工具调用，并输入ProcessingEnviroment参数。
   * ProcessingEnviroment提供很多有用的工具类Elements, Types 和 Filer
   *
   * @param processingEnv 提供给 processor 用来访问工具框架的环境
   */
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    elementTool = processingEnv.getElementUtils();
    messager = processingEnv.getMessager();
    filer = processingEnv.getFiler();
    typeTool = processingEnv.getTypeUtils();

    // 只有接受到 App壳 传递过来的书籍，才能证明我们的 APT环境搭建完成
    options = processingEnv.getOptions().get(ProcessorConfig.OPTIONS);
    aptPackage = processingEnv.getOptions().get(ProcessorConfig.APT_PACKAGE);
    messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> options:" + options);
    messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> aptPackage:" + aptPackage);
    if (options != null && aptPackage != null) {
      messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境搭建完成....");
    } else {
      messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境有问题，请检查 options 与 aptPackage 为null...");
    }
  }

  /**
   * 这相当于每个处理器的主函数main()，你在这里写你的扫描、评估和处理注解的代码，以及生成Java文件。
   * 输入参数RoundEnviroment，可以让你查询出包含特定注解的被注解元素
   *
   * @param annotations 请求处理的注解类型
   * @param roundEnv 有关当前和以前的信息环境
   * @return 如果返回 true，则这些注解已声明并且不要求后续 Processor 处理它们；
   * 如果返回 false，则这些注解未声明并且可能要求后续 Processor 处理它们
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (annotations.isEmpty()) {
      messager.printMessage(Diagnostic.Kind.NOTE, "并没有发现 被@ARouter注解的地方呀");
      return false;// 没有机会处理
    }

    // 获取所有被 @ARouter 注解的 元素集合
    Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);

    // 通过Element工具类，获取Activity，Callback类型
    TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
    // 显示类信息（获取被注解的节点，类节点）这也叫自描述 Mirror
    TypeMirror activityMirror = activityType.asType();
    // TODO 新增点1
    TypeElement callType = elementTool.getTypeElement(ProcessorConfig.CALL);
    TypeMirror callMirror = callType.asType();

    // 遍历所有的类节点
    for (Element element : elements) {// 1 element == MainActivity    2 element == MainActivity2
      // 获取类节点，获取包节点 （com.wuc.xxxxxx）
      String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();

      // 获取简单类名，例如：MainActivity  MainActivity2  MainActivity3
      String className = element.getSimpleName().toString();
      // 拿到注解
      ARouter aRouter = element.getAnnotation(ARouter.class);

      /**
       模块一
       package com.example.helloworld;

       public final class HelloWorld {

       public static void main(String[] args) {
       System.out.println("Hello, JavaPoet!");
       }
       }
       */
      // Java 万物皆对象
      // C  万物皆指针
      //1.方法
      /*MethodSpec mainMethod = MethodSpec.methodBuilder("main")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)//添加修饰符
          .returns(void.class)//返回值
          .addParameter(String[].class, "args")//方法参数
          .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")//设置方法体内容
          .build();
      //2.类
      TypeSpec testClass = TypeSpec.classBuilder("Test" + options)
          .addMethod(mainMethod)
          .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
          .build();
      //3.包
      JavaFile packagef = JavaFile.builder("com.wuc.test", testClass).build();
      //生成文件
      try {
        packagef.writeTo(filer);
      } catch (IOException e) {
        e.printStackTrace();
        messager.printMessage(Diagnostic.Kind.NOTE, "生成Test文件时失败，异常:" + e.getMessage());
      }*/

      /**
       模板：
       public class MainActivity3$$$$$$$$$ARouter {

       public static Class findTargetClass(String path) {
       return path.equals("/app/MainActivity3") ? MainActivity3.class : null;
       }

       }
       */

      /*
      messager.printMessage(Diagnostic.Kind.NOTE, "被@ARetuer注解的类有：" + className);

      // 目标：要生成的文件名称  MainActivity$$$$$$$$$ARouter
      String finalClassName = className + "$$$$$$$$$ARouter";



      //1. 方法
      MethodSpec findTargetClass = MethodSpec.methodBuilder("findTargetClass")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .returns(Class.class)
          .addParameter(String.class, "path")
          // 方法里面的内容 return path.equals("/app/MainActivity3") ? MainActivity3.class : null;
          // 需要JavaPoet包装转型
          .addStatement("return path.equals($S) ? $T.class : null", aRouter.path(), ClassName.get((TypeElement) element))
          .build();
      //2. 类
      TypeSpec myClass = TypeSpec.classBuilder(finalClassName)
          .addMethod(findTargetClass)
          .addModifiers(Modifier.PUBLIC)
          .build();
      //3. 包
      JavaFile packagef = JavaFile.builder(packageName, myClass).build();

      try {
        packagef.writeTo(filer);
      } catch (IOException e) {
        e.printStackTrace();
      } */

      // TODO  一系列的检查工作

      // 在循环里面，对 “路由对象” 进行封装
      RouterBean routerBean = new RouterBean.Builder()
          .addGroup(aRouter.group())
          .addPath(aRouter.path())
          .addElement(element)
          .build();

      // ARouter注解的类 必须继承 Activity
      TypeMirror elementMirror = element.asType();// Main2Activity的具体详情 例如：继承了 Activity
      //测试一种类型是否为另一种的子类型
      if (typeTool.isSubtype(elementMirror, activityMirror)) {// activityMirror  android.app.Activity描述信息
        routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
      } else if (typeTool.isSubtype(elementMirror, callMirror)) {
        routerBean.setTypeEnum(RouterBean.TypeEnum.CALL);// TODO 新增点2
      } else { // Derry.java 的干法 就会抛出异常
        // 不匹配抛出异常，这里谨慎使用！考虑维护问题
        throw new RuntimeException("@ARouter注解目前仅限用于Activity类之上");
      }

      if (checkRouterPath(routerBean)) {
        messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean Check Success:" + routerBean.toString());

        // 赋值 mAllPathMap 集合里面去
        List<RouterBean> routerBeans = mAllPathMap.get(routerBean.getGroup());
        // 如果从Map中找不到key为：bean.getGroup()的数据，就新建List集合再添加进Map
        if (ProcessorUtils.isEmpty(routerBeans)) {
          routerBeans = new ArrayList<>();
          routerBeans.add(routerBean);
          mAllPathMap.put(routerBean.getGroup(), routerBeans);// 加入仓库一
        } else {
          routerBeans.add(routerBean);
        }
      } else { // ERROR 编译期发生异常
        messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
      }
    }// TODO end for 循环结束

    // 定义（生成类文件实现的接口） 有 Path Group
    TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH); // ARouterPath描述
    TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP); // ARouterGroup描述
    // TODO 第一大步：系列PATH
    try {
      createPathFile(pathType);// 生成 Path类
    } catch (IOException e) {
      e.printStackTrace();
      messager.printMessage(Diagnostic.Kind.NOTE, "在生成PATH模板时，异常了 e:" + e.getMessage());
    }

    // TODO 第二大步：组头（带头大哥）
    try {
      createGroupFile(groupType, pathType);
    } catch (IOException e) {
      e.printStackTrace();
      messager.printMessage(Diagnostic.Kind.NOTE, "在生成GROUP模板时，异常了 e:" + e.getMessage());
    }

    return true;
  }

  /**
   * 生成路由组Group文件，如：ARouter$$Group$$app
   *
   * @param groupType ARouterLoadGroup接口信息
   * @param pathType ARouterLoadPath接口信息
   */
  private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
    // 仓库二 缓存二 判断是否有需要生成的类文件
    if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(mAllPathMap)) {
      return;
    }
    // 返回值 这一段 Map<String, Class<? extends ARouterPath>>
    TypeName methodReturns = ParameterizedTypeName.get(
        ClassName.get(Map.class),// Map
        ClassName.get(String.class),// Map<String,

        // Class<? extends ARouterPath>>
        ParameterizedTypeName.get(
            ClassName.get(Class.class),
            // ? extends ARouterPath
            WildcardTypeName.subtypeOf(ClassName.get(pathType))) // ? extends ARouterLoadPath
        // WildcardTypeName.supertypeOf() 做实验 ? super
        // 最终的：Map<String, Class<? extends ARouterPath>>
    );

    // 1.方法 public Map<String, Class<? extends ARouterPath>> getGroupMap()
    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME)//方法名
        .addAnnotation(Override.class) // 重写注解 @Override
        .addModifiers(Modifier.PUBLIC) // public修饰符
        .returns(methodReturns); // 方法返回值

    // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();

    methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
        ClassName.get(Map.class),
        ClassName.get(String.class),
        // Class<? extends ARouterPath>>
        ParameterizedTypeName.get(
            ClassName.get(Class.class),
            // ? extends ARouterPath
            WildcardTypeName.subtypeOf(ClassName.get(pathType))), // ? extends ARouterPath
        // WildcardTypeName.supertypeOf() 做实验 ? super
        // 最终的：Map<String, Class<? extends ARouterPath>>
        ProcessorConfig.GROUP_VAR1,
        ClassName.get(HashMap.class)
    );

    //  groupMap.put("personal", ARouter$$Path$$personal.class);
    //	groupMap.put("order", ARouter$$Path$$order.class);
    for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
      methodBuilder.addStatement("$N.put($S, $T.class)",
          ProcessorConfig.GROUP_VAR1, // groupMap.put
          entry.getKey(), // order, personal ,app
          //根据包名找类
          ClassName.get(aptPackage, entry.getValue()));
    }

    // return groupMap;
    methodBuilder.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

    // 最终生成的类文件名 ARouter$$Group$$ + personal
    String finalClassName = ProcessorConfig.GROUP_FILE_NAME + options;

    messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
        aptPackage + "." + finalClassName);

    // 生成类文件：ARouter$$Group$$app
    JavaFile.builder(aptPackage, // 包名
        TypeSpec.classBuilder(finalClassName) // 类名
            .addSuperinterface(ClassName.get(groupType)) // 实现ARouterGroup接口 implements ARouterGroup
            .addModifiers(Modifier.PUBLIC) // public修饰符
            .addMethod(methodBuilder.build()) // 方法的构建（方法参数 + 方法体）
            .build()) // 类构建完成
        .build() // JavaFile构建完成
        .writeTo(filer); // 文件生成器开始生成类文件
  }

  /**
   * 系列Path的类  生成工作
   *
   * @param pathType ARouterPath 高层的标准
   * @throws IOException d
   */
  private void createPathFile(TypeElement pathType) throws IOException {
    // 判断 map仓库中，是否有需要生成的文件
    if (ProcessorUtils.isEmpty(mAllPathMap)) {
      return;
    }

    // 倒序生成代码

    // 任何的class类型，必须包装
    // Map<String, RouterBean>
    TypeName methodReturn = ParameterizedTypeName.get(
        ClassName.get(Map.class),//Map
        ClassName.get(String.class),//Map<String,
        ClassName.get(RouterBean.class)//Map<String, RouterBean>
    );

    // 遍历仓库 app,order,personal
    for (Map.Entry<String, List<RouterBean>> entry : mAllPathMap.entrySet()) {
      // 1.方法
      MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)//方法名
          .addAnnotation(Override.class)// 给方法上添加注解  @Override
          .addModifiers(Modifier.PUBLIC)// public修饰符
          .returns(methodReturn);// 把Map<String, RouterBean> 加入方法返回

      // Map<String, RouterBean> pathMap = new HashMap<>(); // $N == 变量 为什么是这个，因为变量有引用 所以是$N
      methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
          ClassName.get(Map.class),           // Map
          ClassName.get(String.class),        // Map<String,
          ClassName.get(RouterBean.class),    // Map<String, RouterBean>
          ProcessorConfig.PATH_VAR1,          // Map<String, RouterBean> pathMap
          ClassName.get(HashMap.class)        // Map<String, RouterBean> pathMap = new HashMap<>();
      );

      // 必须要循环，因为有多个
      // pathMap.put("/personal/PersonalMain2Activity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
      // PersonalMain2Activity.class,"/personal/PersonalMain2Activity","personal");
      // pathMap.put("/personal/PersonalMainActivity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY));
      List<RouterBean> pathList = entry.getValue();

      /*
       $N == 变量 变量有引用 所以 N
       $L == TypeEnum.ACTIVITY
       */
      // personal 的细节
      for (RouterBean bean : pathList) {
        methodBuilder.addStatement("$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
            ProcessorConfig.PATH_VAR1, // pathMap.put
            bean.getPath(), // "/personal/PersonalMain2Activity"
            ClassName.get(RouterBean.class), // RouterBean
            ClassName.get(RouterBean.TypeEnum.class), // RouterBean.Type
            bean.getTypeEnum(), // 枚举类型：ACTIVITY
            ClassName.get((TypeElement) bean.getElement()), // MainActivity.class  Main2Activity.class
            bean.getPath(), // 路径名
            bean.getGroup() // 组名
        );
      }// TODO end for

      // return pathMap;
      methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);

      // TODO 注意：不能像以前一样，1.方法，2.类  3.包， 因为这里面有implements ，所以 方法和类要合为一体生成才行，这是特殊情况

      // 最终生成的类文件名  ARouter$$Path$$personal
      String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();
      messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
          aptPackage + "." + finalClassName);

      // 生成类文件：ARouter$$Path$$personal
      JavaFile.builder(aptPackage,// 包名  APT 存放的路径
          TypeSpec.classBuilder(finalClassName)// 类名
              .addSuperinterface(ClassName.get(pathType))// 实现ARouterPath接口  implements ARouterPath==pathType
              .addModifiers(Modifier.PUBLIC)// public修饰符
              .addMethod(methodBuilder.build())// 方法的构建（方法参数 + 方法体）
              .build()// 类构建完成
      ).build()// JavaFile构建完成
          .writeTo(filer);// 文件生成器开始生成类文件
      // 仓库二 缓存二  非常重要一步，注意：PATH 路径文件生成出来了，才能赋值路由组mAllGroupMap
      mAllGroupMap.put(entry.getKey(), finalClassName);
    }//ToDo end for
  }

  /**
   * 校验@ARouter注解的值，如果group未填写就从必填项path中截取数据
   *
   * @param bean 路由详细信息，最终实体封装类
   */
  private boolean checkRouterPath(RouterBean bean) {
    //"app"   "order"   "personal"
    String group = bean.getGroup();
    //"/app/MainActivity"   "/order/OrderMainActivity"   "/personal/PersonalMainActivity"
    String path = bean.getPath();
    // 校验
    // @ARouter注解中的path值，必须要以 / 开头（模仿阿里Arouter规范）
    if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
      messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
      return false;
    }
    // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
    if (path.lastIndexOf("/") == 0) {
      // 架构师定义规范，让开发者遵循
      messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
      return false;
    }
    // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app,order,personal 作为group
    String finalGroup = path.substring(1, path.indexOf("/", 1));

    // app,order,personal == options
    // @ARouter注解中的group有赋值情况
    if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
      // 架构师定义规范，让开发者遵循
      messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
      return false;
    } else {
      bean.setGroup(finalGroup);
    }

    // 如果真的返回ture   RouterBean.group  xxxxx 赋值成功 没有问题
    return true;
  }
}
