using buoi5.Models;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Hosting.Server;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Internal;
using System;
using System.IO;
using System.Linq;

using System.Web;
using static System.Net.WebRequestMethods;

namespace buoi5.Controllers
{
    public class HanghoaController : Controller
    {
        private QLBHContext db = new QLBHContext();
        Models.QLBHContext obj = new Models.QLBHContext();
      
        public IActionResult Index()
        {
            var hanghoa = db.Hanghoa.Include(h => h.MaloaiNavigation)
                .Include(h => h.MansxNavigation);
            return View(hanghoa.ToList());
        }
        public IActionResult chiTietHanghoa(string id)
        {
            
            var a = obj.Hanghoa.Include(h => h.MaloaiNavigation).Include(h => h.MansxNavigation)
                .Where(n => n.Mahang == id).FirstOrDefault();
            return View(a);


        }
        public IActionResult formThemHH()
        {
            ViewBag.DSLHH = new SelectList(db.Loaihanghoa.ToList(), "Maloai", "Maloai");
            ViewBag.DSNSX = new SelectList(db.Nhasanxuat.ToList(), "Mansx", "Mansx");
            return View();
        }
        
       
        [HttpPost]
        [ValidateAntiForgeryToken]
        public ActionResult ThemHH([FromForm] Models.HangHoaModel hanghoa )
        {
            Hanghoa n = db.Hanghoa.Find(hanghoa.Mahang);
                          
            if (n == null && ModelState.IsValid)
            {               
                ViewBag.n = null;
                Hanghoa h = new Hanghoa();
                h.Mahang = hanghoa.Mahang;
                h.Tenhang = hanghoa.Tenhang;
                h.Dongia = hanghoa.Dongia;
                h.Donvitinh = hanghoa.Donvitinh;
                h.Maloai = hanghoa.Maloai;
                h.Mansx = hanghoa.Mansx;
                h.MaloaiNavigation = db.Loaihanghoa.Find(hanghoa.Mansx);
                h.MansxNavigation = db.Nhasanxuat.Find(hanghoa.Maloai);
                
                if(hanghoa.Hinh.Length>0)
                {
                    var path = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "images", hanghoa.Mahang + "_" + hanghoa.Hinh.FileName);
                    using (var s = System.IO.File.Create(path))
                    {
                        hanghoa.Hinh.CopyTo(s);
                    }
                    h.Hinh = "~/images/" + hanghoa.Mahang + "_" + hanghoa.Hinh.FileName;
                }
                else
                {
                    h.Hinh = "";
                }

                db.Hanghoa.Add(h);
                db.SaveChanges();
                
                return RedirectToAction("Index");
            }
            else if (n != null)
            {
                ViewBag.nv = n;
               
                return View("loiThemHH", hanghoa);
            }
            else
            {
                ViewBag.n = null;
                ViewBag.DSLHH = new SelectList(db.Loaihanghoa.ToList(), "Maloai", "Maloai");
                ViewBag.DSNSX = new SelectList(db.Nhasanxuat.ToList(), "Mansx", "Mansx");
                return View("formThemHH");
            }
        }
        [HttpGet]
        public ActionResult loiThemHH(Models.Hanghoa hanghoa)
        {
            ViewBag.DSLHH = new SelectList(db.Loaihanghoa.ToList(), "Maloai", "Maloai");
            ViewBag.DSNSX = new SelectList(db.Nhasanxuat.ToList(), "Mansx", "Mansx");
            return View("formThemHH", hanghoa);
        }


        public IActionResult xemNsx(string id)
        {
            Nhasanxuat x = db.Nhasanxuat.Find(id); 

            return PartialView(x);
        }
        public IActionResult xemLHH(string id)
        {
            Loaihanghoa x = db.Loaihanghoa.Find(id); 

            return PartialView(x);
        }


        public IActionResult formXoaHH(string id)
        {
            int dem = db.Loaihanghoa.Where(a => a.Maloai == id).ToList().Count; ///loc ra Msmh trung voi id
            ViewBag.flag = dem;
            Models.Hanghoa x = db.Hanghoa.Find(id);
            return View(x);
        }
        public IActionResult XoaHH(string id)
        {
            Models.Hanghoa x = db.Hanghoa.Find(id);
            if (x != null)
            {
                db.Hanghoa.Remove(x);
                db.SaveChanges();
            }
            return RedirectToAction("Index"); /// goi ham index de view lai
        }

        public IActionResult formsuaHH(string id)
        {
            Models.Hanghoa mh = db.Hanghoa.Find(id);
            Models.Hanghoa x = new Models.Hanghoa
            {
                Mahang = mh.Mahang,
                Tenhang = mh.Tenhang,
                Donvitinh = mh.Donvitinh,
                Dongia = mh.Dongia,
                Mansx = mh.Mansx,
                Maloai = mh.Maloai

            };
            ViewBag.DSLHH = new SelectList(db.Loaihanghoa.ToList(), "Maloai", "Maloai");
            ViewBag.DSNSX = new SelectList(db.Nhasanxuat.ToList(), "Mansx", "Mansx");
            return View(x);
        }

        public IActionResult suaHH(Models.Hanghoa mh)
        {
            if (ModelState.IsValid) //ktra gui dc hay ko dc tra ve gia tri true fales
            {
                Models.Hanghoa x = db.Hanghoa.Find(mh.Mahang);
                if (x != null)
                {
                    x.Tenhang = mh.Tenhang;
                    x.Donvitinh = mh.Donvitinh;
                    x.Dongia = mh.Dongia;
                    x.Mansx = mh.Mansx;
                    x.Maloai = mh.Maloai;
                    ViewBag.DSLHH = new SelectList(db.Loaihanghoa.ToList(), "Maloai", "Maloai");
                    ViewBag.DSNSX = new SelectList(db.Nhasanxuat.ToList(), "Mansx", "Mansx");
                    db.SaveChanges();
                }
                return RedirectToAction("Index", "Hanghoa");
            }
            else
            {
                return View("formsuaHH");

            }
        }



    }
}
