package com.harana.play.osgi.service

import java.io.File
import java.net.URL
import java.nio.file.{Files, Paths}
import java.util

import javax.inject.{Inject, Singleton}
import org.apache.felix.framework.Felix
import org.osgi.framework.{Bundle, BundleContext, Constants}
import play.api.{Configuration, Environment, Logger}

import scala.collection.mutable.{ListBuffer => MutableList}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

@Singleton
class OSGIService @Inject()(configuration: Configuration, environment: Environment, lifecycle: ApplicationLifecycle) {

  val systemBundleDir = environment.getFile("bundles")
  val pluginsDir = environment.getFile("plugins")
  val osgiCacheDir = environment.getFile("felix-cache")

  private def installSystemBundles(bundleContext: BundleContext): Unit = {
    val systemBundles = new MutableList[Bundle]()
    if (systemBundleDir.exists()) {
      systemBundleDir.listFiles
        .filter(_.isFile)
        .filter(_.getName.endsWith("jar"))
        .foreach { bundleJar => systemBundles += bundleContext.installBundle("file:" + bundleJar.getAbsolutePath)
      }
      for (bundle <- systemBundles) {
        bundle.start()
      }
    }
  }

  val felix = {
    val props = new util.HashMap[String, String]
    props.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "scala, scalaz, org.osgi, org.apache.felix, play")
    props.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT)
    props.put(Constants.FRAMEWORK_STORAGE, osgiCacheDir.toString)
    props.put("felix.shutdown.hook", "true")
    props.put("felix.service.urlhandlers", "true")
    props.put("felix.fileinstall.dir", pluginsDir.getAbsolutePath)
    props.put("felix.fileinstall.noInitialDelay", "true")
    props.put("felix.fileinstall.log.level", "4")

    val felix = new Felix(props)
    felix.start()
    felix
  }

  lazy val bundleContext = {
    val bundleContext = felix.getBundleContext
    installSystemBundles(bundleContext)
    felix.init()
    bundleContext
  }

  lifecycle.addStopHook { () =>
    felix.stop()
  }

  def getResource(className: String, resourcePath: String): Future[Option[URL]] = Future {
    bundleContext.getBundles.find { bundle =>
      bundle.getEntry(className.replace(".", "/") + ".class") != null
    }.map(_.getEntry(resourcePath))
  }

  def installPlugin(bundleLocation: String) = {
    Logger.info("Installing plugin: " + bundleLocation)
    val installedBundle = bundleContext.installBundle(bundleLocation)
    installedBundle.start()
  }

  def uninstallPlugin(bundleLocation: String)   = {
    bundleContext.getBundles
      .filter(_.getLocation == bundleLocation)
      .foreach { bundle =>
        Logger.info("Uninstalling plugin: " + bundleLocation)
        bundle.uninstall()
      }
  }

  def removePlugin(pluginName: String) = {
    pluginsDir.listFiles
      .filter(_.isFile)
      .filter(_.getName == pluginName)
      .foreach { file =>
        if (file.exists()) {
          Logger.info("Removing plugin: " + file.getName)
          file.delete()
        }
      }
  }

  def copyPlugin(filePath: String) = {
    val originFile = new File(filePath)
    Files.copy(Paths.get(filePath), Paths.get(pluginsDir + "/" + originFile.getName))
  }

	def getBundles: Future[List[Bundle]] = Future {
		bundleContext.getBundles
	}

	def findPluginClasses[T <: U, U](implicit cmf: ClassTag[T]): Future[List[(T, ServiceReference[_])]] = Future {
		val serviceReferences = bundleContext.getAllServiceReferences(cmf.runtimeClass.getName, null)
		if (serviceReferences == null) Map()
		else {
      serviceReferences.map { serviceRef =>
        val service = bundleContext.getService(serviceRef).asInstanceOf[T]
        (service, serviceRef)
      }
		}
  }
}