-- ==============================================================================
-- PREPAI BD — Seed Data for Supabase PostgreSQL
-- ==============================================================================

-- 1. Exam Categories
INSERT INTO public.exam_categories (id, code, name_en, name_bn, description, display_order)
VALUES
    ('cat_bcs', 'BCS', 'Bangladesh Civil Service (BCS)', 'বাংলাদেশ সিভিল সার্ভিস (বিসিএস)', 'Preliminary & Written preparation for general and technical cadres', 1),
    ('cat_bank', 'BANK', 'Bangladesh Bank & Combined Banks', 'বাংলাদেশ ব্যাংক ও সমন্বিত ব্যাংক', 'AD, Senior Officer and Officer recruitment exams', 2),
    ('cat_primary', 'PRIMARY', 'Primary & NTRCA Teacher', 'প্রাথমিক সহকারী শিক্ষক ও শিক্ষক নিবন্ধন', 'Primary school teacher recruitment and NTRCA preparation', 3),
    ('cat_railway', 'GOVT', 'Railway & 10th-20th Grade Jobs', 'রেলওয়ে ও মন্ত্রণালয় গ্রেড ১০-২০', 'All government ministry and department recruitment tests', 4)
ON CONFLICT (id) DO NOTHING;

-- 2. Initial Sample Questions
INSERT INTO public.questions (
    id, exam_category, subject, topic, question_en, question_bn,
    options_en, options_bn, correct_index,
    explanation_en, explanation_bn, ai_shortcut, difficulty, previous_year_tag, status
)
VALUES
    (
        'q_bcs_01', 'BCS', 'Bangla', 'Bangla Literature',
        'Who is the author of ''Charyapada'', the earliest extant work in Bengali literature?',
        'বাংলা সাহিত্যের প্রাচীনতম নিদর্শন ‘চর্যাপদ’-এর আদি পদকর্তা বা রচয়িতা কে?',
        '["Luipa", "Kanhapa", "Bhushukupa", "Shabarpa"]'::jsonb,
        '["লুইপা", "কাহ্নপা", "ভূসুকুপা", "শবরপা"]'::jsonb,
        0,
        'Luipa is traditionally regarded as the first poet (Adi Kobi) of Charyapada. He composed the first song ''Ka''''a tarubara pancha bi dala''.',
        'চর্যাপদের প্রথম পদটির রচয়িতা লুইপা। তাকে চর্যাপদের আদি কবি বা সিদ্ধাচার্য হিসেবে গণ্য করা হয়। চর্যাপদের সর্বাধিক পদ রচনা করেন কাহ্নপা (১৩টি)।',
        'লুইপা = প্রথম পদকর্তা (পদ ১)। কাহ্নপা = সর্বাধিক পদ (১৩টি পদ)।',
        'EASY', '44th BCS Preliminary', 'APPROVED'
    ),
    (
        'q_bcs_02', 'BCS', 'Bangladesh Affairs', 'Constitution & Liberation War',
        'Under which article of the Constitution of Bangladesh is the Right to Life and Personal Liberty guaranteed?',
        'বাংলাদেশের সংবিধানের কোন অনুচ্ছেদে ''জীবন ও ব্যক্তি স্বাধীনতার অধিকার'' নিশ্চিত করা হয়েছে?',
        '["Article 31", "Article 32", "Article 36", "Article 39"]'::jsonb,
        '["অনুচ্ছেদ ৩১", "অনুচ্ছেদ ৩২", "অনুচ্ছেদ ৩৬", "অনুচ্ছেদ ৩৯"]'::jsonb,
        1,
        'Article 32 provides: ''No person shall be deprived of life or personal liberty, save in accordance with law.''',
        'সংবিধানের ৩২ নম্বর অনুচ্ছেদে বলা হয়েছে, আইনানুযায়ী ব্যতীত জীবন ও ব্যক্তি-স্বাধীনতা হইতে কোন ব্যক্তিকে বঞ্চিত করা যাইবে না। ৩১ অনুচ্ছেদ আইনের আশ্রয়লাভের অধিকার এবং ৩৯ বাক ও ভাব প্রকাশের স্বাধীনতা।',
        '৩২ = জীবন ও ব্যক্তি স্বাধীনতা; ৩১ = আইনের আশ্রয়; ৩৯ = মতপ্রকাশ ও বাকস্বাধীনতা।',
        'MEDIUM', '45th BCS Preliminary', 'APPROVED'
    ),
    (
        'q_bank_01', 'BANK', 'Mathematics', 'Percentage & Profit-Loss',
        'If the price of sugar increases by 25%, by what percent must a family reduce consumption to keep expenditure unchanged?',
        'চিনির মূল্য ২৫% বৃদ্ধি পেলে, একটি পরিবার চিনির ব্যবহার শতকরা কত কমালে খরচ একই থাকবে?',
        '["15%", "20%", "25%", "33.33%"]'::jsonb,
        '["১৫%", "২০%", "২৫%", "৩৩.৩৩%"]'::jsonb,
        1,
        'Reduction % = [r / (100 + r)] * 100 = [25 / 125] * 100 = (1/5) * 100 = 20%.',
        'শর্টকাট সূত্র: হ্রাসকৃত হার = [r / (১০০ + r)] × ১০০% = [২৫ / ১২৫] × ১০০% = ২০%। সুতরাং খরচ অপরিবর্তিত রাখতে ব্যবহার ২০% কমাতে হবে।',
        'Shortcut Formula: [R / (100 + R)] * 100%. If R=25%, Answer is always 20%.',
        'MEDIUM', 'Bangladesh Bank AD 2022', 'APPROVED'
    ),
    (
        'q_bank_02', 'BANK', 'English', 'Prepositions & Idioms',
        'The committee decided to defer the decision ______ next Monday.',
        'কমিটি আগামী সোমবার পর্যন্ত সিদ্ধান্ত স্থগিত রাখার সিদ্ধান্ত নিয়েছে। শূন্যস্থানে উপযুক্ত Preposition কোনটি?',
        '["until", "for", "at", "in"]'::jsonb,
        '["until", "for", "at", "in"]'::jsonb,
        0,
        '''Defer to / until'' means postpone or put off to a later time.',
        '''Defer until'' বা ''defer to'' অর্থ কোনো সময় পর্যন্ত স্থগিত রাখা। এখানে নির্দিষ্ট দিন (next Monday) পর্যন্ত সময়ের সীমা বুঝাতে ''until'' ব্যবহৃত হয়।',
        'Defer + until / to a future point in time.',
        'MEDIUM', 'Combined 9 Banks Officer 2023', 'APPROVED'
    ),
    (
        'q_primary_01', 'PRIMARY', 'Bangla', 'Bangla Grammar (সন্ধি ও সমাস)',
        'What is the correct Sandhi split for ''চলচ্চিত্র'' (Chalachchitra)?',
        '''চলচ্চিত্র'' শব্দের সঠিক সন্ধি বিচ্ছেদ কোনটি?',
        '["চলৎ + চিত্র", "চল + চিত্র", "চলচ্ + চিত্র", "চলতি + চিত্র"]'::jsonb,
        '["চলৎ + চিত্র", "চল + চিত্র", "চলচ্ + চিত্র", "চলতি + চিত্র"]'::jsonb,
        0,
        'According to Bangla consonant Sandhi rules: ৎ/দ্ + চ = চ্চ. Hence চলৎ + চিত্র = চলচ্চিত্র।',
        'ব্যঞ্জনসন্ধির নিয়ম অনুযায়ী, ত্/দ্-এর পর চ থাকলে ত্/দ্ স্থানে চ হয় এবং দুটি মিলে ''চ্চ'' হয়। তাই চলৎ + চিত্র = চলচ্চিত্র।',
        'ত্ + চ = চ্চ (যেমন: চলৎ + চিত্র = চলচ্চিত্র, উৎ + চারণ = উচ্চারণ)।',
        'EASY', 'Primary Teacher Exam 2023', 'APPROVED'
    ),
    (
        'q_bcs_03', 'BCS', 'International Affairs', 'Global Institutions & Geopolitics',
        'Where is the headquarters of the Asian Infrastructure Investment Bank (AIIB) situated?',
        'এশীয় অবকাঠামো বিনিয়োগ ব্যাংক (AIIB)-এর সদর দপ্তর কোথায় অবস্থিত?',
        '["Shanghai", "Beijing", "Manila", "Tokyo"]'::jsonb,
        '["সাংহাই", "বেইজিং", "ম্যানিলা", "টোকিও"]'::jsonb,
        1,
        'AIIB headquarters is in Beijing, China. (Note: NDB/BRICS bank is in Shanghai, ADB is in Manila).',
        'AIIB-এর সদর দপ্তর চীনের বেইজিংয়ে। নিউ ডেভেলপমেন্ট ব্যাংক (NDB)-এর সদর দপ্তর সাংহাই এবং এশীয় উন্নয়ন ব্যাংক (ADB)-এর সদর দপ্তর ম্যানিলা, ফিলিপাইন।',
        'AIIB = বেইজিং; NDB (ব্রিকস) = সাংহাই; ADB = ম্যানিলা।',
        'EASY', '43rd BCS Preliminary', 'APPROVED'
    )
ON CONFLICT (id) DO NOTHING;

-- 3. Mock Exams
INSERT INTO public.mock_exams (
    id, title_en, title_bn, exam_category, duration_minutes, total_marks,
    negative_mark_per_wrong, question_ids, is_adaptive, status
)
VALUES
    (
        'mock_bcs_full',
        '46th BCS Preliminary Model Test #01',
        '৪৬তম বিসিএস প্রিলিমিনারি পূর্ণাঙ্গ মডেল টেস্ট - ০১',
        'BCS', 120, 200, 0.5,
        '["q_bcs_01", "q_bcs_02", "q_bcs_03", "q_bank_01", "q_bank_02", "q_primary_01"]'::jsonb,
        true, 'APPROVED'
    ),
    (
        'mock_bank_ad',
        'Bangladesh Bank AD Speed Sprint (Math & English)',
        'বাংলাদেশ ব্যাংক এডি স্পিড টেস্ট (ম্যাথ ও ইংলিশ)',
        'BANK', 45, 80, 0.25,
        '["q_bank_01", "q_bank_02", "q_bcs_03"]'::jsonb,
        false, 'APPROVED'
    )
ON CONFLICT (id) DO NOTHING;

-- 4. Job Circulars
INSERT INTO public.job_circulars (
    id, organization_en, organization_bn, job_title_en, job_title_bn,
    category, vacancy_count, qualification, age_limit, deadline, apply_url,
    match_status, match_notes
)
VALUES
    (
        'circ_46_bcs',
        'Bangladesh Public Service Commission (BPSC)',
        'বাংলাদেশ সরকারি কর্ম কমিশন (বিপিএসসি)',
        '46th BCS Examination (General & Technical Cadre)',
        '৪৬তম বিসিএস পরীক্ষা (সাধারণ ও কারিগরি ক্যাডার)',
        'BCS & PSC', 3140, 'Graduation / Post Graduation from recognized university',
        '21 to 32 years (Relaxed quota applied)', '15 October 2026', 'https://bpsc.teletalk.com.bd',
        'SUITABLE', 'Matches your CS graduation and age criteria. Target exam syllabus mapped.'
    ),
    (
        'circ_bb_ad',
        'Bangladesh Bank (Central Bank)',
        'বাংলাদেশ ব্যাংক (সেন্ট্রাল ব্যাংক)',
        'Assistant Director (General)',
        'সহকারী পরিচালক (জেনারেল)',
        'Bank', 225, 'Four-year Bachelor / Master''s degree in any discipline',
        '30 years', '28 September 2026', 'https://erecruitment.bb.org.bd',
        'SUITABLE', 'Strong profile match. High competition in Mathematics and Analytical ability.'
    )
ON CONFLICT (id) DO NOTHING;
