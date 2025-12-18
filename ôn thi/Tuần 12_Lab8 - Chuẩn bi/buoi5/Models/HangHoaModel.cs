using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Http;

namespace buoi5.Models
{
    public class HangHoaModel
    {
        [Required(ErrorMessage = "Xin nhập mã hàng hóa ")]// rang buoc nhap dl
        [Display(Name = "Mã hàng hóa")]
        public string Mahang { get; set; }
        [Required(ErrorMessage = "Xin nhập tên hàng hóa ")]// rang buoc nhap dl
        [Display(Name = "Tên hàng hóa")]
        public string Tenhang { get; set; }
        [Required(ErrorMessage = "Xin nhập đơn vị tính ")]// rang buoc nhap dl
        [Display(Name = "Đơn vị tính")]
        public string Donvitinh { get; set; }
        [Required(ErrorMessage = "Xin nhập đơn giá ")]// rang buoc nhap dl
        [Display(Name = "Đơn giá")]
        public double? Dongia { get; set; }

        [Display(Name = "Hình ảnh")]
        public IFormFile Hinh { get; set; }
        [Display(Name = "Mã loại")]
        public string Maloai { get; set; }
        [Display(Name = "Mã nhà sản xuất")]
        public string Mansx { get; set; }
    }
}
