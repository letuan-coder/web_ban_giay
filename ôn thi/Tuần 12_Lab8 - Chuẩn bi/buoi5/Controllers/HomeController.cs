using buoi5.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Newtonsoft.Json;

namespace buoi5.Controllers
{
    public class HomeController : Controller
    {
        QLBHContext db = new QLBHContext();

        public HomeController()
        {

        }

        public IActionResult Index()
        {
            ViewBag.dshh = db.Hanghoa.ToList();
            return View();
        }
        
        public ActionResult chonHanghoa(string id)
        {
            if (string.IsNullOrEmpty(id))
                return View();
            Hanghoa hh = db.Hanghoa.Find(id);
            int sl = Convert.ToInt32(Request.Form["txtSoluong"]);

            string json = HttpContext.Session.GetString("phieudathang");
            Phieudathang pdh;
            if (json != null)
            {
                pdh = JsonConvert.DeserializeObject<Phieudathang>(json);
            }
            else
            {               
                pdh = new Phieudathang();
            }
            Chitietphieudathang ct = pdh.Chitietphieudathang.SingleOrDefault(c => c.Mahang == hh.Mahang);
            if (ct == null)
            {
                ct = new Chitietphieudathang();
                ct.Mahang = hh.Mahang;
                ct.Dongia = hh.Dongia;
                ct.Soluong = sl;
                ct.MahangNavigation = hh;
                pdh.Chitietphieudathang.Add(ct);
            }
            else
            {
                ct.Soluong += sl;
            }

            string json1 = JsonConvert.SerializeObject(pdh);
            HttpContext.Session.SetString("phieudathang", json1);
            
            return View();
        }
    }
}
