-- ==============================================================================
-- PREPAI BD — Comprehensive PostgreSQL Database Schema for Supabase
-- Phase 2 Backend Architecture: Authentication, Database, and Row Level Security
-- ==============================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. ENUM TYPES
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('STUDENT', 'ADMIN', 'INSTRUCTOR');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE question_difficulty AS ENUM ('EASY', 'MEDIUM', 'HARD');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE review_status AS ENUM ('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE match_status AS ENUM ('SUITABLE', 'REVIEW_NEEDED', 'NOT_ELIGIBLE');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE subscription_tier AS ENUM ('FREE', 'BASIC', 'PREMIUM');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 3. PROFILES TABLE (Linked to auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role user_role NOT NULL DEFAULT 'STUDENT',
    phone TEXT,
    avatar_url TEXT,
    education TEXT,
    university TEXT,
    graduation_year TEXT,
    target_exam TEXT NOT NULL DEFAULT '46th BCS Preliminary',
    target_exam_date TEXT DEFAULT 'Nov 2026',
    daily_study_hours INTEGER NOT NULL DEFAULT 4,
    streak_days INTEGER NOT NULL DEFAULT 0,
    xp_points INTEGER NOT NULL DEFAULT 0,
    level INTEGER NOT NULL DEFAULT 1,
    readiness_score INTEGER NOT NULL DEFAULT 0,
    preferred_language VARCHAR(5) NOT NULL DEFAULT 'BN',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. EXAM CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS public.exam_categories (
    id TEXT PRIMARY KEY,
    name_en TEXT NOT NULL,
    name_bn TEXT NOT NULL,
    code TEXT UNIQUE NOT NULL,
    description TEXT,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. SUBJECTS TABLE
CREATE TABLE IF NOT EXISTS public.subjects (
    id TEXT PRIMARY KEY,
    exam_category_id TEXT REFERENCES public.exam_categories(id) ON DELETE CASCADE,
    code TEXT NOT NULL,
    name_en TEXT NOT NULL,
    name_bn TEXT NOT NULL,
    total_marks NUMERIC(5, 2) DEFAULT 0,
    weight_percentage NUMERIC(5, 2) DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. TOPICS TABLE
CREATE TABLE IF NOT EXISTS public.topics (
    id TEXT PRIMARY KEY,
    subject_id TEXT REFERENCES public.subjects(id) ON DELETE CASCADE,
    name_en TEXT NOT NULL,
    name_bn TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. QUESTIONS TABLE (Central Question Bank)
CREATE TABLE IF NOT EXISTS public.questions (
    id TEXT PRIMARY KEY,
    exam_category TEXT NOT NULL,
    subject TEXT NOT NULL,
    topic TEXT NOT NULL,
    question_en TEXT NOT NULL,
    question_bn TEXT NOT NULL,
    options_en JSONB NOT NULL DEFAULT '[]'::jsonb,
    options_bn JSONB NOT NULL DEFAULT '[]'::jsonb,
    correct_index INTEGER NOT NULL,
    explanation_en TEXT NOT NULL,
    explanation_bn TEXT NOT NULL,
    ai_shortcut TEXT,
    difficulty question_difficulty NOT NULL DEFAULT 'MEDIUM',
    previous_year_tag TEXT,
    status review_status NOT NULL DEFAULT 'APPROVED',
    is_from_ai_review BOOLEAN DEFAULT false,
    created_by UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_questions_category ON public.questions(exam_category);
CREATE INDEX IF NOT EXISTS idx_questions_subject ON public.questions(subject);
CREATE INDEX IF NOT EXISTS idx_questions_status ON public.questions(status);

-- 8. BOOKMARKS TABLE
CREATE TABLE IF NOT EXISTS public.bookmarks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    question_id TEXT NOT NULL REFERENCES public.questions(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_bookmarks_user ON public.bookmarks(user_id);

-- 9. STUDY TASKS TABLE
CREATE TABLE IF NOT EXISTS public.study_tasks (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title_en TEXT NOT NULL,
    title_bn TEXT NOT NULL,
    subject TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL DEFAULT 30,
    is_completed BOOLEAN NOT NULL DEFAULT false,
    is_overdue BOOLEAN NOT NULL DEFAULT false,
    priority VARCHAR(20) NOT NULL DEFAULT 'HIGH',
    due_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tasks_user ON public.study_tasks(user_id);

-- 10. WEAKNESS ITEMS TABLE
CREATE TABLE IF NOT EXISTS public.weakness_items (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    subject TEXT NOT NULL,
    topic TEXT NOT NULL,
    accuracy_percent INTEGER NOT NULL,
    mistake_count INTEGER NOT NULL DEFAULT 0,
    recommendation_en TEXT NOT NULL,
    recommendation_bn TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_weakness_user ON public.weakness_items(user_id);

-- 11. MOCK EXAMS TABLE
CREATE TABLE IF NOT EXISTS public.mock_exams (
    id TEXT PRIMARY KEY,
    title_en TEXT NOT NULL,
    title_bn TEXT NOT NULL,
    exam_category TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    total_marks INTEGER NOT NULL,
    negative_mark_per_wrong REAL NOT NULL DEFAULT 0.5,
    question_ids JSONB NOT NULL DEFAULT '[]'::jsonb,
    is_adaptive BOOLEAN NOT NULL DEFAULT false,
    status review_status NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 12. EXAM ATTEMPTS TABLE
CREATE TABLE IF NOT EXISTS public.exam_attempts (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    exam_id TEXT NOT NULL REFERENCES public.mock_exams(id) ON DELETE CASCADE,
    exam_title TEXT NOT NULL,
    score REAL NOT NULL,
    total_questions INTEGER NOT NULL,
    correct_count INTEGER NOT NULL,
    wrong_count INTEGER NOT NULL,
    skipped_count INTEGER NOT NULL,
    accuracy_percent INTEGER NOT NULL,
    time_spent_seconds INTEGER NOT NULL,
    selected_answers JSONB NOT NULL DEFAULT '{}'::jsonb,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_attempts_user ON public.exam_attempts(user_id);

-- 13. JOB CIRCULARS TABLE
CREATE TABLE IF NOT EXISTS public.job_circulars (
    id TEXT PRIMARY KEY,
    organization_en TEXT NOT NULL,
    organization_bn TEXT NOT NULL,
    job_title_en TEXT NOT NULL,
    job_title_bn TEXT NOT NULL,
    category TEXT NOT NULL,
    vacancy_count INTEGER NOT NULL DEFAULT 1,
    qualification TEXT NOT NULL,
    age_limit TEXT NOT NULL,
    deadline TEXT NOT NULL,
    apply_url TEXT NOT NULL,
    match_status match_status NOT NULL DEFAULT 'SUITABLE',
    match_notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    published_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 14. AI CONVERSATIONS & MESSAGES TABLE
CREATE TABLE IF NOT EXISTS public.ai_conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL DEFAULT 'Study Session',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.ai_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL REFERENCES public.ai_conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    is_user BOOLEAN NOT NULL,
    message_text TEXT NOT NULL,
    tutor_mode TEXT NOT NULL DEFAULT 'SIMPLE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 15. USER SUBSCRIPTIONS TABLE (Monetization Foundation)
CREATE TABLE IF NOT EXISTS public.subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    plan_tier subscription_tier NOT NULL DEFAULT 'FREE',
    start_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expiry_date TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ==============================================================================
-- 16. ROW LEVEL SECURITY (RLS) POLICIES
-- ==============================================================================

-- Enable RLS on all sensitive tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.questions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bookmarks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.study_tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.weakness_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.mock_exams ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.exam_attempts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.job_circulars ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;

-- Helper function: Check if current user has ADMIN role
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = auth.uid() AND role = 'ADMIN'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- PROFILES POLICIES
CREATE POLICY "Users can view their own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id OR public.is_admin());

CREATE POLICY "Users can update their own permitted profile fields"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Admins can manage all profiles"
    ON public.profiles FOR ALL
    USING (public.is_admin());

-- QUESTIONS POLICIES
CREATE POLICY "Students can view approved questions"
    ON public.questions FOR SELECT
    USING (status = 'APPROVED' OR public.is_admin());

CREATE POLICY "Admins can manage questions"
    ON public.questions FOR ALL
    USING (public.is_admin());

-- BOOKMARKS POLICIES
CREATE POLICY "Users manage their own bookmarks"
    ON public.bookmarks FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- STUDY TASKS POLICIES
CREATE POLICY "Users manage their own tasks"
    ON public.study_tasks FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- WEAKNESS ITEMS POLICIES
CREATE POLICY "Users manage their own weakness items"
    ON public.weakness_items FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- MOCK EXAMS POLICIES
CREATE POLICY "Anyone can view approved mock exams"
    ON public.mock_exams FOR SELECT
    USING (status = 'APPROVED' OR public.is_admin());

CREATE POLICY "Admins can manage mock exams"
    ON public.mock_exams FOR ALL
    USING (public.is_admin());

-- EXAM ATTEMPTS POLICIES
CREATE POLICY "Users manage their own exam attempts"
    ON public.exam_attempts FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- JOB CIRCULARS POLICIES
CREATE POLICY "Anyone can view active circulars"
    ON public.job_circulars FOR SELECT
    USING (is_active = true OR public.is_admin());

CREATE POLICY "Admins can manage circulars"
    ON public.job_circulars FOR ALL
    USING (public.is_admin());

-- AI CONVERSATIONS POLICIES
CREATE POLICY "Users manage their own AI conversations"
    ON public.ai_conversations FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users manage their own AI messages"
    ON public.ai_messages FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- SUBSCRIPTIONS POLICIES
CREATE POLICY "Users can view their own subscription"
    ON public.subscriptions FOR SELECT
    USING (auth.uid() = user_id OR public.is_admin());

-- ==============================================================================
-- 17. AUTOMATIC USER PROFILE PROVISIONING TRIGGER
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (
        id,
        email,
        full_name,
        role,
        target_exam,
        daily_study_hours,
        streak_days,
        xp_points,
        level,
        readiness_score,
        preferred_language
    ) VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', 'New Student'),
        'STUDENT',
        COALESCE(NEW.raw_user_meta_data->>'target_exam', '46th BCS Preliminary'),
        4,
        1,
        100,
        1,
        45,
        'BN'
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger firing on auth.users creation
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
