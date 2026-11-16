window.TESTS_DATA=[
  {
    id:'repo',
    title:'المستودع والهيكل',
    tag:'',
    items:[
      {id:'repo-1',what:'الفرعان main وdev موجودان على GitHub',how:'افتح المستودع على GitHub وتحقق من قائمة الفروع.'},
      {id:'repo-2',what:'main يطابق dev',how:'على GitHub افتح Compare بين main وdev، ويجب ألا يظهر فرق.'},
      {id:'repo-3',what:'أول commit في main هو README فقط',how:'افتح سجل الـcommits لفرع main وانظر أقدم commit.'},
      {id:'repo-4',what:'commit مستقل لكل ملف في dev',how:'افتح سجل dev وتحقق أن كل commit يحوي ملفاً واحداً.'},
      {id:'repo-5',what:'ملفات Word داخل docs لا تُرفع',how:'ضع ملفاً اسمه a.docx داخل docs/word ثم شغّل git status. يجب ألا يظهر.'},
      {id:'repo-6',what:'الرفع العادي لا يشغل أي بناء',how:'ارفع تعديلاً صغيراً إلى dev وافتح تبويب Actions. يجب ألا يظهر تشغيل جديد.'},
      {id:'repo-7',what:'دفع tag يشغل البناء ويفشل برسالة gradlew not found',how:'شغّل tools\\scripts\\tag.bat بالاسم v0.0.1-test.1. يتوقع فشل البناء بهذه الرسالة لأن مشروع Gradle لم يُنشأ بعد.'},
      {id:'repo-8',what:'سكربت tag يرفض صيغة خاطئة',how:'شغّل tag.bat بالاسم test1. يجب أن يرفضه.'},
      {id:'repo-9',what:'سكربت tag يرفض tag مستقراً من فرع dev',how:'شغّل tag.bat بالاسم v1.0.0 وأنت على dev. يجب أن يرفضه.'}
    ]
  }
];
