using Microsoft.AspNetCore.Mvc;
using System.Linq;
using System;
using buoi5.Models;
using Microsoft.EntityFrameworkCore.Metadata.Internal;
namespace buoi5.Controllers
{
    public class nhasanxuatController : Controller
    {


        QLBHContext db = new QLBHContext();
        public IActionResult Index()
        {
            ViewBag.nsx = db.Nhasanxuat;
            return View();
        }
        [HttpGet]
        public IActionResult themNSX()
        {
            return View();
        }
        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult themNSX([Bind(include: "Mansx,Tennsx,Diachi")] Models.Nhasanxuat nhasanxuat)
        {
            Nhasanxuat n = db.Nhasanxuat.Find(nhasanxuat.Mansx);
            if (n == null && ModelState.IsValid)
            {
                ViewBag.n = null;
                db.Nhasanxuat.Add(nhasanxuat);
                db.SaveChanges();
                return RedirectToAction("Index");
            }
            else if (n != null)
            {
                ViewBag.nv = n;
                return View("loiThemNSX", nhasanxuat);

            }
            else
            {
                ViewBag.n = null;
                return View("themNSX");
            }



        }


        [HttpGet]
        public ActionResult loiThemNSX(Models.Nhasanxuat nhasanxuat)
        {


            return View("themNSX", nhasanxuat);
        }
        public IActionResult formxoaNSX(string id)
        {
            int dem = db.Hanghoa.Where(a => a.Mansx == id).ToList().Count; ///loc ra Msmh trung voi id
            ViewBag.flag = dem;
            Models.Nhasanxuat x = db.Nhasanxuat.Find(id);
            return View(x);
        }
       
        public IActionResult xoaNSX(string id)
        {
            Models.Nhasanxuat x = db.Nhasanxuat.Find(id);
            if (x != null)
            {
                db.Nhasanxuat.Remove(x);
                db.SaveChanges();
            }
            return RedirectToAction("Index", "nhasanxuat");
        }
        public IActionResult formsuaNSX(string id)
        {
            Models.Nhasanxuat nsx = db.Nhasanxuat.Find(id);
            Models.Nhasanxuat x = new Models.Nhasanxuat
            {
                Mansx = nsx.Mansx,
                Tennsx = nsx.Tennsx,
                Diachi = nsx.Diachi

            };
            return View(x);
        }
        public IActionResult suaNSX(Models.Nhasanxuat nsx)
        {
            if (ModelState.IsValid) //ktra gui dc hay ko dc tra ve gia tri true fales
            {
                Models.Nhasanxuat x = db.Nhasanxuat.Find(nsx.Mansx);
                if (x != null)
                {
                    x.Tennsx = nsx.Tennsx;
                    x.Diachi = nsx.Diachi;
                    db.SaveChanges();
                }
                return RedirectToAction("Index","nhasanxuat");
            }
            else
            {
                return View("formsuaNSX");

            }
        }





    }

}